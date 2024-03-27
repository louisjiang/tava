package io.tava.db;

import com.github.benmanes.caffeine.cache.*;
import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.db.segment.*;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import io.tava.function.Function1;
import io.tava.lang.Option;
import io.tava.lang.Tuple4;
import io.tava.lock.HashReadWriteLock;
import io.tava.serialization.Serialization;
import io.tava.util.Util;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDatabase implements Database, Util, CacheLoader<Database.DBKey, Object>, RemovalListener<Database.DBKey, Object> {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HashReadWriteLock<String> readWriteLock = new HashReadWriteLock<>();
    protected final Map<String, Map<String, Operation>> tableNameToOperationMap = new ConcurrentHashMap<>();
    private final Map<String, Long> tableNameToTimestamps = new ConcurrentHashMap<>();
    private final byte[] EMPTY = new byte[0];
    private final Serialization serialization;
    private final int batchSize;
    private final int interval;
    private final int initialCapacity;
    private final ForkJoinPool forkJoinPool;
    private final LoadingCache<DBKey, Object> cache;

    protected AbstractDatabase(Configuration configuration, Serialization serialization) {
        this.serialization = serialization;
        this.batchSize = configuration.getInt("batch_size", 2048);
        this.interval = configuration.getInt("interval", 10000);
        this.initialCapacity = this.batchSize / 2;
        this.forkJoinPool = new ForkJoinPool(configuration.getInt("fork-join-pool-parallelism", Runtime.getRuntime().availableProcessors() * 3));
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        int initialCapacity = configuration.getInt("cache.initial-capacity");
        int maximumSize = configuration.getInt("cache.maximum-size");
        int expireAfterAccess = configuration.getInt("cache.expire-after-access");
        caffeine.initialCapacity(initialCapacity);
        caffeine.maximumSize(maximumSize);
        caffeine.expireAfterAccess(expireAfterAccess, TimeUnit.SECONDS);
        caffeine.recordStats();
        caffeine.softValues();
        caffeine.removalListener(this);
        this.cache = caffeine.build(this);
    }

    @Override
    public Cache<DBKey, Object> cache() {
        return cache;
    }

    @Override
    public <V> SegmentList<V> newSegmentList(String tableName, String key, int capacity) {
        return new SegmentArrayList<>(AbstractDatabase.this, tableName, key, capacity);
    }

    @Override
    public <V> Option<SegmentList<V>> getSegmentList(String tableName, String key) {
        return Option.option(SegmentList.get(AbstractDatabase.this, tableName, key));
    }

    @Override
    public <V> SegmentSet<V> newSegmentSet(String tableName, String key, int segment) {
        return new SegmentHashSet<>(AbstractDatabase.this, tableName, key, segment);
    }

    @Override
    public <V> Option<SegmentSet<V>> getSegmentSet(String tableName, String key) {
        return Option.option(SegmentSet.get(AbstractDatabase.this, tableName, key));
    }

    @Override
    public <K, V> SegmentMap<K, V> newSegmentMap(String tableName, String key, int segment) {
        return new SegmentHashMap<>(AbstractDatabase.this, tableName, key, segment);
    }

    @Override
    public <K, V> Option<SegmentMap<K, V>> getSegmentMap(String tableName, String key) {
        return Option.option(SegmentMap.get(AbstractDatabase.this, tableName, key));
    }

    @Override
    public void put(String tableName, String key, Object value) {
        this.writeLock(tableName, key, () -> {
            this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).put(value);
        });
        this.cache.put(new DBKey(tableName, key), value);
    }

    @Override
    public void delete(String tableName, String key) {
        this.writeLock(tableName, key, () -> {
            this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).delete();
            this.cache.invalidate(new DBKey(tableName, key));
        });
    }

    @Override
    public <T> T update(String tableName, String key, Function1<T, T> update) {
        return this.writeLock(tableName, key, () -> {
            T value = get(tableName, key);
            value = update.apply(value);
            put(tableName, key, value);
            return value;
        });
    }

    @Override
    public <T> T get(String tableName, String key) {
        return (T) this.cache.get(new DBKey(tableName, key));
    }

    @Override
    public void tryCommit(String tableName) {
        commit(tableName, false);
    }

    @Override
    public void commit(String tableName) {
        commit(tableName, true);
    }

    private void commit(String tableName, boolean force) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        if (operationMap == null || operationMap.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long timestamp = tableNameToTimestamps.computeIfAbsent(tableName, s -> 0L);
        if (!force && operationMap.size() < this.batchSize && timestamp + interval > now) {
            return;
        }

        int totalBytes = 0;
        Map<byte[], byte[]> puts = new HashMap<>();
        Set<byte[]> deletes = new HashSet<>();

        Map<String, Operation> commits = new ConcurrentHashMap<>(operationMap.size());
        List<ForkJoinTask<Tuple4<byte[], byte[], String, Operation>>> tasks = new ArrayList<>();
        for (Map.Entry<String, Operation> entry : operationMap.entrySet()) {
            String key = entry.getKey();
            String lockKey = toString(tableName, "@", key);
            if (this.readWriteLock.isWriteLocked(lockKey) || this.readWriteLock.isReadLocked(lockKey)) {
                continue;
            }

            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            Operation operation = entry.getValue();
            if (operation.isDelete()) {
                this.readWriteLock.writeLock(lockKey);
                deletes.add(bytesKey);
                commits.put(key, operation);
                this.readWriteLock.unWriteLock(lockKey);
                continue;
            }

            ForkJoinTask<Tuple4<byte[], byte[], String, Operation>> task = this.forkJoinPool.submit(() -> {
                byte[] bytes = this.readWriteLock.doWithWriteLock(lockKey, () -> this.toBytes(operation.getValue()));
                return Tava.of(bytesKey, bytes, key, operation);
            });
            tasks.add(task);
        }
        for (ForkJoinTask<Tuple4<byte[], byte[], String, Operation>> task : tasks) {
            try {
                Tuple4<byte[], byte[], String, Operation> tuple3 = task.get();
                byte[] bytes = tuple3.getValue2();
                if (bytes == EMPTY) {
                    continue;
                }
                totalBytes += bytes.length;
                puts.put(tuple3.getValue1(), bytes);
                commits.put(tuple3.getValue3(), tuple3.getValue4());
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        long elapsedTime = System.currentTimeMillis() - now;
        this.commit(tableName, puts, deletes, totalBytes);
        int changed = 0;
        for (Map.Entry<String, Operation> entry : commits.entrySet()) {
            String key = entry.getKey();
            Operation value = entry.getValue();
            boolean flag = operationMap.remove(key, value);
            if (!flag) {
                changed++;
            }
        }
        timestamp = System.currentTimeMillis();
        tableNameToTimestamps.put(tableName, timestamp);
        logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}][{}]", path(), tableName, puts.size(), deletes.size(), changed, byteToString(totalBytes), elapsedTime, timestamp - now);
    }

    protected abstract List<byte[]> get(String tableName, List<byte[]> keys);

    protected abstract byte[] get(String tableName, byte[] key);

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes, int totalBytes);

    @Override
    public boolean dropTable(String tableName) {
        this.tableNameToOperationMap.remove(tableName);
        this.tableNameToTimestamps.remove(tableName);
        return true;
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tableNames = new HashSet<>();
        tableNames.addAll(this.tableNameToOperationMap.keySet());
        tableNames.addAll(this.tableNameToTimestamps.keySet());
        return tableNames;
    }

    @Override
    public byte[] toBytes(Object value) {
        try {
            return this.serialization.toBytes(value);
        } catch (Exception cause) {
            this.logger.error("toBytes", cause);
            return EMPTY;
        }
    }

    @Override
    public Object toObject(byte[] bytes) {
        try {
            return this.serialization.toObject(bytes);
        } catch (Exception cause) {
            this.logger.error("toObject", cause);
            return null;
        }
    }

    @Override
    public void readLock(String tableName, String key, Consumer0 consumer) {
        this.readWriteLock.doWithReadLock(toString(tableName, "@", key), consumer);
    }

    public <T> T readLock(String tableName, String key, Function0<T> function) {
        return this.readWriteLock.doWithReadLock(toString(tableName, "@", key), function);
    }

    @Override
    public void writeLock(String tableName, String key) {
        this.readWriteLock.writeLock(toString(tableName, "@", key));
    }

    @Override
    public void unWriteLock(String tableName, String key) {
        this.readWriteLock.unWriteLock(toString(tableName, "@", key));
    }

    @Override
    public void readLock(String tableName, String key) {
        this.readWriteLock.readLock(toString(tableName, "@", key));
    }

    @Override
    public void unReadLock(String tableName, String key) {
        this.readWriteLock.unReadLock(toString(tableName, "@", key));
    }

    public void writeLock(String tableName, String key, Consumer0 consumer) {
        this.readWriteLock.doWithWriteLock(toString(tableName, "@", key), consumer);
    }

    @Override
    public <T> T writeLock(String tableName, String key, Function0<T> function) {
        return this.readWriteLock.doWithWriteLock(toString(tableName, "@", key), function);
    }


    @Override
    public Object load(DBKey dbKey) throws Exception {
        String tableName = dbKey.tableName();
        String key = dbKey.key();
        return readLock(tableName, key, () -> {
            Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
            Operation operation;
            if (operationMap != null && (operation = operationMap.get(key)) != null) {
                return operation.getValue();
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return this.toObject(bytes);
        });
    }

    @Override
    public void onRemoval(@Nullable DBKey key, @Nullable Object value, RemovalCause cause) {
    }

    @Override
    public Iterator iterator(String tableName, boolean useSnapshot) {
        return null;
    }

    @Override
    public boolean keyMayExist(String tableName, String key) {
        return false;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public boolean createTable(String tableName) {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void compact(String tableName) {

    }

    @Override
    public void compact() {

    }

    private String byteToString(int byteLength) {
        if (byteLength < 1024) {
            return byteLength + "B";
        }
        byteLength = byteLength / 1024;
        if (byteLength < 1024) {
            return byteLength + "KB";
        }
        byteLength = byteLength / 1024;
        if (byteLength < 1024) {
            return byteLength + "MB";
        }
        byteLength = byteLength / 1024;
        return byteLength + "GB";
    }

    public static class Operation {

        private int version = 0;
        private boolean delete;
        private Object value;

        public synchronized void delete() {
            if (delete) {
                return;
            }
            this.delete = true;
            this.value = null;
            this.version++;
        }

        public synchronized void put(Object value) {
            if (value == null) {
                this.delete();
                return;
            }
            this.delete = false;
            this.value = value;
            this.version++;
        }

        public int getVersion() {
            return version;
        }

        public boolean isDelete() {
            return delete;
        }

        public Object getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Operation operation = (Operation) o;
            return version == operation.version && delete == operation.delete && Objects.equals(value, operation.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, delete, value);
        }
    }

}

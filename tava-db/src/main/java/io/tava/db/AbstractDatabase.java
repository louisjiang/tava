package io.tava.db;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.tava.configuration.Configuration;
import io.tava.db.segment.*;
import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lock.HashReadWriteLock;
import io.tava.serialization.Serialization;
import io.tava.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
@SuppressWarnings("unchecked")
public abstract class AbstractDatabase implements Database, Util {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final HashReadWriteLock<String> readWriteLock = new HashReadWriteLock<>();
    protected final Map<String, Map<String, Operation>> tableNameToOperationMap = new ConcurrentHashMap<>();
    private final Map<String, Consumer3<String, byte[], byte[]>> putCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Consumer2<String, byte[]>> deleteCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Long> tableNameToTimestamps = new ConcurrentHashMap<>();
    private final byte[] EMPTY = new byte[0];
    private final Serialization serialization;
    private final int batchSize;
    private final int interval;
    private final int initialCapacity;
    private final Cache<String, Segment> cache;

    protected AbstractDatabase(Configuration configuration, Serialization serialization) {
        this.serialization = serialization;
        this.batchSize = configuration.getInt("batch_size", 2048);
        this.interval = configuration.getInt("interval", 10000);
        this.initialCapacity = this.batchSize / 2;
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder();
        caffeine.initialCapacity(512);
        caffeine.maximumSize(2048);
        caffeine.expireAfterAccess(60, TimeUnit.SECONDS);
        caffeine.softValues();
        caffeine.recordStats();
        this.cache = caffeine.build();
    }

    @Override
    public <V> SegmentList<V> newSegmentList(String tableName, String key, int capacity) {
        return (SegmentList<V>) this.cache.get(toString("list@", tableName, "@", key), s -> new SegmentArrayList<V>(AbstractDatabase.this, tableName, key, capacity));
    }

    @Override
    public <V> Option<SegmentList<V>> getSegmentList(String tableName, String key) {
        return Option.option((SegmentList<V>) this.cache.get(toString("list@", tableName, "@", key), s -> SegmentList.get(AbstractDatabase.this, tableName, key)));
    }

    @Override
    public <V> SegmentSet<V> newSegmentSet(String tableName, String key, int segment) {
        return (SegmentSet<V>) this.cache.get(toString("set@", tableName, "@", key), s -> new SegmentHashSet<>(AbstractDatabase.this, tableName, key, segment));
    }

    @Override
    public <V> Option<SegmentSet<V>> getSegmentSet(String tableName, String key) {
        return Option.option((SegmentSet<V>) this.cache.get(toString("set@", tableName, "@", key), s -> SegmentSet.get(AbstractDatabase.this, tableName, key)));
    }

    @Override
    public <K, V> SegmentMap<K, V> newSegmentMap(String tableName, String key, int segment) {
        return (SegmentMap<K, V>) this.cache.get(toString("map@", tableName, "@", key), s -> new SegmentHashMap<>(AbstractDatabase.this, tableName, key, segment));
    }

    @Override
    public <K, V> Option<SegmentMap<K, V>> getSegmentMap(String tableName, String key) {
        return Option.option((SegmentMap<K, V>) this.cache.get(toString("map@", tableName, "@", key), s -> SegmentMap.get(AbstractDatabase.this, tableName, key)));
    }

    @Override
    public void put(String tableName, String key, Object value) {
        this.writeLock(key, () -> this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).put(value));
    }

    @Override
    public void delete(String tableName, String key) {
        this.writeLock(key, () -> this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).delete());
    }

    @Override
    public <T> T update(String tableName, String key, Function1<T, T> update) {
        return this.writeLock(key, () -> {
            T value = get(tableName, key);
            value = update.apply(value);
            put(tableName, key, value);
            return value;
        });
    }

    @Override
    public <T> T get(String tableName, String key) {
        return readLock(key, () -> {
            Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
            Operation operation;
            if (operationMap != null && (operation = operationMap.get(key)) != null) {
                return (T) operation.getValue();
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            return (T) this.toObject(bytes);
        });
    }

    @Override
    public <T> void get(String tableName, String key, Consumer1<T> consumer1) {
        this.readLock(key, () -> {
            Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
            Operation operation;
            if (operationMap != null && (operation = operationMap.get(key)) != null) {
                T value = (T) operation.getValue();
                consumer1.accept(value);
                return value;
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            T value = (T) this.toObject(bytes);
            consumer1.accept(value);
            return value;
        });

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

        long totalBytes = 0;
        Map<byte[], byte[]> puts = new HashMap<>();
        Set<byte[]> deletes = new HashSet<>();
        Consumer3<String, byte[], byte[]> putCallback = this.putCallbacks.get(tableName);
        Consumer2<String, byte[]> deleteCallback = this.deleteCallbacks.get(tableName);

        Map<String, Operation> commits = new HashMap<>(operationMap.size());
        for (Map.Entry<String, Operation> entry : operationMap.entrySet()) {
            String key = entry.getKey();
            if (this.readWriteLock.isWriteLocked(key)) {
                continue;
            }
            this.readWriteLock.writeLock(key);
            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            totalBytes += bytesKey.length;
            Operation operation = entry.getValue();
            if (operation.isDelete()) {
                deletes.add(bytesKey);
                commits.put(key, operation);
                if (deleteCallback != null) {
                    deleteCallback.accept(key, bytesKey);
                }
                this.readWriteLock.unWriteLock(key);
                continue;
            }
            byte[] bytesValue = this.toBytes(operation.getValue());
            if (bytesValue == EMPTY) {
                this.readWriteLock.unWriteLock(key);
                continue;
            }
            totalBytes += bytesValue.length;
            puts.put(bytesKey, bytesValue);
            commits.put(key, operation);
            if (putCallback != null) {
                putCallback.accept(key, bytesKey, bytesValue);
            }
            this.readWriteLock.unWriteLock(key);
        }
        long elapsedTime = System.currentTimeMillis() - now;
        this.commit(tableName, puts, deletes);
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

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes);

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
    public void readLock(String key, Consumer0 consumer) {
        this.readWriteLock.doWithReadLock(key, consumer);
    }

    public <T> T readLock(String key, Function0<T> function) {
        return this.readWriteLock.doWithReadLock(key, function);
    }

    @Override
    public void writeLock(String key) {
        this.readWriteLock.writeLock(key);
    }

    @Override
    public void unWriteLock(String key) {
        this.readWriteLock.unWriteLock(key);
    }

    @Override
    public void readLock(String key) {
        this.readWriteLock.readLock(key);
    }

    @Override
    public void unReadLock(String key) {
        this.readWriteLock.unReadLock(key);
    }

    public void writeLock(String key, Consumer0 consumer) {
        this.readWriteLock.doWithWriteLock(key, consumer);
    }

    @Override
    public <T> T writeLock(String key, Function0<T> function) {
        return this.readWriteLock.doWithWriteLock(key, function);
    }

    private String byteToString(long byteLength) {
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

    @Override
    public void addCommitCallback(String tableName, Consumer3<String, byte[], byte[]> putCallback, Consumer2<String, byte[]> deleteCallback) {
        if (putCallback != null) {
            this.putCallbacks.put(tableName, putCallback);
        }
        if (deleteCallback != null) {
            this.deleteCallbacks.put(tableName, deleteCallback);
        }
    }

    @Override
    public void updateSegmentCache(String key, Segment segment) {
        this.cache.put(key, segment);
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

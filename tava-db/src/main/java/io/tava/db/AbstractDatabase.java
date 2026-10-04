package io.tava.db;

import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.db.segment.*;
import io.tava.function.Function1;
import io.tava.lang.Option;
import io.tava.lang.Tuple3;
import io.tava.lang.Tuple4;
import io.tava.lock.SegmentLock;
import io.tava.serialization.kryo.Serialization;
import io.tava.util.NamedThreadFactory;
import io.tava.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
public abstract class AbstractDatabase implements Database, Util {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Map<String, Map<String, Operation>> tableNameToOperationMap = new ConcurrentHashMap<>();
    private final SegmentLock<String> segmentLock = new SegmentLock<>(256);
    private final byte[] EMPTY = new byte[0];
    private final Serialization serialization;
    private final int initialCapacity = 1024;
    private final ThreadPoolExecutor threadPoolExecutor;
    private final int batchSize;
    private final long interval;
    private final Map<String, Long> commitTimestamps = new ConcurrentHashMap<>();
    private final long maxCommitSize;

    protected AbstractDatabase(Configuration configuration, Serialization serialization) {
        this.serialization = serialization;
        this.batchSize = configuration.getInt("batch-size");
        this.interval = configuration.getLong("interval");
        this.maxCommitSize = configuration.getMemorySize("max-commit-size").toBytes();
        this.threadPoolExecutor = new ThreadPoolExecutor(configuration.getInt("core-pool-size"), configuration.getInt("maximum-pool-size"), 1, TimeUnit.MINUTES, new LinkedBlockingQueue<>(512), new NamedThreadFactory("rocksdb", true), new ThreadPoolExecutor.CallerRunsPolicy());
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
        this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).put(value);
//        tryCommit(tableName);
    }

    @Override
    public void delete(String tableName, String key) {
        this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).delete();
//        tryCommit(tableName);
    }

    @Override
    public <T> T update(String tableName, String key, Function1<T, T> update) {
        T value = get(tableName, key);
        value = update.apply(value);
        put(tableName, key, value);
        return value;
    }

    @Override
    public <T> T get(String tableName, String key) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        Operation operation;
        if (operationMap != null && (operation = operationMap.get(key)) != null) {
            return (T) operation.getValue();
        }
        byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return (T) this.toObject(tableName, key, bytes);
    }

    @Override
    public <T> Map<String, T> getMap(String tableName, List<String> keys) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);

        Map<String, T> map = new HashMap<>();
        List<byte[]> byteKeys = new ArrayList<>();
        List<String> keys0 = new ArrayList<>();

        Operation operation;
        for (String key : keys) {
            if (operationMap != null && (operation = operationMap.get(key)) != null) {
                map.put(key, (T) operation.getValue());
                continue;
            }
            keys0.add(key);
            byteKeys.add(key.getBytes(StandardCharsets.UTF_8));
        }

        if (byteKeys.size() > 0) {
            List<byte[]> values = get(tableName, byteKeys);
            int size = values.size();
            for (int i = 0; i < size; i++) {
                String key = keys0.get(i);
                byte[] bytes = values.get(i);
                if (bytes == null || bytes.length == 0) {
                    map.put(key, null);
                    continue;
                }
                T t = (T) this.toObject(tableName, key, bytes);
                map.put(key, t);
            }
        }

        return map;
    }

    @Override
    public void tryCommit(String tableName) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        if (operationMap == null) {
            return;
        }
        if (operationMap.size() >= this.batchSize) {
            commit(tableName);
            return;
        }

        Long commitTimestamp = this.commitTimestamps.get(tableName);
        if (commitTimestamp == null) {
            commit(tableName);
            return;
        }

        long now = System.currentTimeMillis();
        if (now - commitTimestamp >= this.interval) {
            commit(tableName);
        }
    }

    @Override
    public void commit(String tableName) {
        this.segmentLock.doWithLock(tableName, () -> commitLock(tableName));
    }

    private void commitLock(String tableName) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        if (operationMap == null || operationMap.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        this.commitTimestamps.put(tableName, now);
        Set<byte[]> deletes = new HashSet<>();
        Map<String, Integer> deleteVersions = new HashMap<>();
        List<Future<Tuple4<byte[], byte[], String, Integer>>> futures = new ArrayList<>();
        for (Map.Entry<String, Operation> entry : operationMap.entrySet()) {
            String key = entry.getKey();
            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            Operation operation = entry.getValue();
            if (operation.isDelete()) {
                deletes.add(bytesKey);
                deleteVersions.put(key, operation.getVersion());
                continue;
            }

            Future<Tuple4<byte[], byte[], String, Integer>> future = this.threadPoolExecutor.submit(() -> {
                int version = operation.getVersion();
                byte[] bytesValue = this.toBytes(tableName, key, operation.getValue());
                return Tava.of(bytesKey, bytesValue, key, version);
            });
            futures.add(future);
        }
        List<Tuple3<Map<byte[], byte[]>, Map<String, Integer>, Integer>> batch = new ArrayList<>();
        Map<String, Integer> putVersions = new HashMap<>();
        Map<byte[], byte[]> puts = new HashMap<>();
        int totalBytes = 0;
        for (Future<Tuple4<byte[], byte[], String, Integer>> future : futures) {
            try {
                Tuple4<byte[], byte[], String, Integer> tuple4 = future.get();
                byte[] bytesValue = tuple4.getValue2();
                if (bytesValue == EMPTY) {
                    continue;
                }
                putVersions.put(tuple4.getValue3(), tuple4.getValue4());
                puts.put(tuple4.getValue1(), bytesValue);
                totalBytes += bytesValue.length;
                if (totalBytes >= this.maxCommitSize) {
                    batch.add(Tava.of(puts, putVersions, totalBytes));
                    putVersions = new HashMap<>();
                    puts = new HashMap<>();
                    totalBytes = 0;
                }
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }
        if (puts.size() > 0) {
            batch.add(Tava.of(puts, putVersions, totalBytes));
        }

        long elapsedTime = System.currentTimeMillis() - now;

        if (batch.isEmpty()) {
            this.commit(tableName, new HashMap<>(), deletes, 1024);
            for (Map.Entry<String, Integer> entry : deleteVersions.entrySet()) {
                String key = entry.getKey();
                Integer version = entry.getValue();
                Operation operation = operationMap.get(key);
                if (operation == null) {
                    continue;
                }
                if (operation.getVersion() == version) {
                    operationMap.remove(key);
                }
            }
            return;
        }

        for (Tuple3<Map<byte[], byte[]>, Map<String, Integer>, Integer> tuple3 : batch) {
            Map<byte[], byte[]> value1 = tuple3.getValue1();
            Integer value3 = tuple3.getValue3();
            this.commit(tableName, value1, deletes, value3);

            int changed = 0;
            Map<String, Integer> versions = tuple3.getValue2();
            for (Map.Entry<String, Integer> entry : versions.entrySet()) {
                String key = entry.getKey();
                Integer version = entry.getValue();
                Operation operation = operationMap.get(key);
                if (operation == null) {
                    continue;
                }
                if (operation.getVersion() == version) {
                    operationMap.remove(key);
                    continue;
                }
                changed++;
            }

            for (Map.Entry<String, Integer> entry : deleteVersions.entrySet()) {
                String key = entry.getKey();
                Integer version = entry.getValue();
                Operation operation = operationMap.get(key);
                if (operation == null) {
                    continue;
                }
                if (operation.getVersion() == version) {
                    operationMap.remove(key);
                    continue;
                }
                changed++;
            }

            logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}][{}]", path(), tableName, value1.size(), deletes.size(), changed, byteToString(value3), elapsedTime, System.currentTimeMillis() - now);
            deleteVersions.clear();
            versions.clear();
            value1.clear();
            deletes.clear();
        }

    }

    protected abstract List<byte[]> get(String tableName, List<byte[]> keys);

    protected abstract byte[] get(String tableName, byte[] key);

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes, int totalBytes);

    @Override
    public boolean dropTable(String tableName) {
        this.tableNameToOperationMap.remove(tableName);
        return true;
    }

    @Override
    public byte[] toBytes(String tableName, String key, Object value) {
        try {
            return this.serialization.toBytes(value);
        } catch (Exception cause) {
            this.logger.warn("toBytes:{},{}", tableName, key, cause);
            return EMPTY;
        }
    }

    @Override
    public Object toObject(String tableName, String key, byte[] bytes) {
        try {
            return this.serialization.toObject(bytes);
        } catch (Exception cause) {
            this.logger.error("toObject:{}:{}", tableName, key, cause);
            return null;
        }
    }

    protected String byteToString(long byteLength) {
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
        private final AtomicInteger version = new AtomicInteger(0);
        private boolean delete;
        private Object value;

        public boolean delete() {
            if (delete) {
                return true;
            }
            int currentVersion = version.get();
            this.delete = true;
            this.value = null;
            return this.version.compareAndSet(currentVersion, currentVersion + 1);
        }

        public boolean put(Object value) {
            if (value == null) {
                this.delete();
                return true;
            }
            int currentVersion = version.get();
            this.delete = false;
            this.value = value;
            return this.version.compareAndSet(currentVersion, currentVersion + 1);
        }

        public int getVersion() {
            return version.get();
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
            return (version.get() == operation.version.get()) && delete == operation.delete && Objects.equals(value, operation.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, delete, value);
        }
    }

    @Override
    public ThreadPoolExecutor threadPoolExecutor() {
        return this.threadPoolExecutor;
    }
}

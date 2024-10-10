package io.tava.db;

import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.db.segment.*;
import io.tava.function.Function1;
import io.tava.lang.Option;
import io.tava.lang.Tuple4;
import io.tava.serialization.kryo.KryoSerializationPool;
import io.tava.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
public abstract class AbstractDatabase implements Database, Util {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    protected final Map<String, Map<String, Operation>> tableNameToOperationMap = new ConcurrentHashMap<>();
    private final byte[] EMPTY = new byte[0];
    private final KryoSerializationPool serialization;
    private final int initialCapacity = 1024;
    private final ForkJoinPool forkJoinPool;

    protected AbstractDatabase(Configuration configuration, KryoSerializationPool serialization) {
        this.serialization = serialization;
        this.forkJoinPool = new ForkJoinPool(configuration.getInt("fork-join-pool-parallelism", Runtime.getRuntime().availableProcessors() * 3));
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
        this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).put(value);
    }

    @Override
    public void delete(String tableName, String key) {
        this.tableNameToOperationMap.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).computeIfAbsent(key, s -> new Operation()).delete();
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
        return (T) this.toObject(bytes);
    }

    @Override
    public void commit(String tableName) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        if (operationMap == null || operationMap.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        int totalBytes = 0;
        Map<byte[], byte[]> puts = new HashMap<>();
        Set<byte[]> deletes = new HashSet<>();
        Map<String, Integer> versions = new HashMap<>();
        List<ForkJoinTask<Tuple4<byte[], byte[], String, Operation>>> tasks = new ArrayList<>();
        for (Map.Entry<String, Operation> entry : operationMap.entrySet()) {
            String key = entry.getKey();
            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            Operation operation = entry.getValue();
            versions.put(key, operation.getVersion());
            if (operation.isDelete()) {
                deletes.add(bytesKey);
                continue;
            }

            ForkJoinTask<Tuple4<byte[], byte[], String, Operation>> task = this.forkJoinPool.submit(() -> {
                byte[] bytes = this.toBytes(operation.getValue());
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
            } catch (InterruptedException | ExecutionException ignored) {
            }
        }

        long elapsedTime = System.currentTimeMillis() - now;
        this.commit(tableName, puts, deletes, totalBytes);
        int changed = 0;
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
        logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}][{}]", path(), tableName, puts.size(), deletes.size(), changed, byteToString(totalBytes), elapsedTime, System.currentTimeMillis() - now);
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
    public Set<String> getTableNames() {
        return new HashSet<>(this.tableNameToOperationMap.keySet());
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
            return version.equals(operation.version) && delete == operation.delete && Objects.equals(value, operation.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(version, delete, value);
        }
    }

}

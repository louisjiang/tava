package io.tava.db;

import io.tava.configuration.Configuration;
import io.tava.function.Consumer0;
import io.tava.function.Consumer2;
import io.tava.function.Consumer3;
import io.tava.function.Function0;
import io.tava.serialization.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
public abstract class AbstractDatabase implements Database {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Map<String, Consumer3<String, byte[], byte[]>> putCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Consumer2<String, byte[]>> deleteCallbacks = new ConcurrentHashMap<>();
    private final List<ReadWriteLock> locks = new ArrayList<>();
    private final Map<String, Map<String, Operation>> tableNameToOperationMap = new ConcurrentHashMap<>();
    private final Map<String, Long> tableNameToTimestamps = new ConcurrentHashMap<>();
    private final Serialization serialization;
    private final int batchSize;
    private final int interval;
    private final int initialCapacity;

    protected AbstractDatabase(Configuration configuration, Serialization serialization) {
        this.serialization = serialization;
        this.batchSize = configuration.getInt("batch_size", 2048);
        this.interval = configuration.getInt("interval", 10000);
        this.initialCapacity = this.batchSize / 2;
        int lockSize = configuration.getInt("lock_size", 128);
        for (int i = 0; i < lockSize; i++) {
            this.locks.add(new ReentrantReadWriteLock());
        }
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
        Map<byte[], byte[]> putBytes = new HashMap<>();
        Set<byte[]> deleteBytes = new HashSet<>();
        Consumer3<String, byte[], byte[]> putCallback = this.putCallbacks.get(tableName);
        Consumer2<String, byte[]> deleteCallback = this.deleteCallbacks.get(tableName);

        Map<String, Integer> versions = new HashMap<>(operationMap.size());
        for (Map.Entry<String, Operation> entry : operationMap.entrySet()) {
            String key = entry.getKey();
            this.writeLock(key).lock();
            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            totalBytes += bytesKey.length;
            Operation operation = entry.getValue();
            versions.put(key, operation.getVersion());
            if (operation.isDelete()) {
                deleteBytes.add(bytesKey);
                if (deleteCallback != null) {
                    deleteCallback.accept(key, bytesKey);
                }
                this.writeLock(key).unlock();
                continue;
            }
            byte[] bytesValue = this.toBytes(operation.getValue());
            totalBytes += bytesValue.length;
            putBytes.put(bytesKey, bytesValue);
            if (putCallback != null) {
                putCallback.accept(key, bytesKey, bytesValue);
            }
            this.writeLock(key).unlock();
        }
        long elapsedTime = System.currentTimeMillis() - now;
        this.commit(tableName, putBytes, deleteBytes);
        int count = 0;
        for (Map.Entry<String, Integer> entry : versions.entrySet()) {
            String key = entry.getKey();
            this.writeLock(key).lock();
            Operation operation = operationMap.get(key);
            if (operation != null && operation.getVersion() == entry.getValue()) {
                count++;
                operationMap.remove(key);
            }
            this.writeLock(key).unlock();
        }
        timestamp = System.currentTimeMillis();
        tableNameToTimestamps.put(tableName, timestamp);
        logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}][{}]", path(), tableName, putBytes.size(), deleteBytes.size(), versions.size() - count, byteToString(totalBytes), elapsedTime, timestamp - now);
    }

    protected abstract List<byte[]> get(String tableName, List<byte[]> keys);

    protected abstract byte[] get(String tableName, byte[] key);

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes);

    @Override
    public Lock writeLock(String key) {
        return this.locks.get(indexFor(hash(key))).writeLock();
    }

    @Override
    public Lock readLock(String key) {
        return this.locks.get(indexFor(hash(key))).readLock();
    }

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
            return null;
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

    public <T> T readLock(String key, Function0<T> function) {
        try {
            this.readLock(key).lock();
            return function.apply();
        } finally {
            this.readLock(key).unlock();
        }
    }

    public void writeLock(String key, Consumer0 consumer) {
        try {
            this.writeLock(key).lock();
            consumer.accept();
        } finally {
            this.writeLock(key).unlock();
        }
    }

    @Override
    public <T> T writeLock(String key, Function0<T> function) {
        try {
            this.writeLock(key).lock();
            return function.apply();
        } finally {
            this.writeLock(key).unlock();
        }
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

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private int indexFor(int h) {
        return h & (this.locks.size() - 1);
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

    static class Operation {

        private int version = 0;
        private boolean delete;
        private Object value;

        private void delete() {
            this.delete = true;
            this.value = null;
            this.version++;
        }

        public void put(Object value) {
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

    }

}

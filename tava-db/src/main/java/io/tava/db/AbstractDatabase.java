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
    private final Object DELETE_FLAG = new Object();
    private final Map<String, Consumer3<String, byte[], byte[]>> putCallbacks = new ConcurrentHashMap<>();
    private final Map<String, Consumer2<String, byte[]>> deleteCallbacks = new ConcurrentHashMap<>();
    private final List<ReadWriteLock> locks = new ArrayList<>();
    private final Map<String, Map<String, Object>> tableNameToPuts = new ConcurrentHashMap<>();
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
        int lockSize = configuration.getInt("lock_size", 256);
        for (int i = 0; i < lockSize; i++) {
            this.locks.add(new ReentrantReadWriteLock());
        }
    }

    @Override
    public void put(String tableName, String key, Object value) {
        writeLock(key, () -> {
            this.tableNameToPuts.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).put(key, value);
        });
    }

    @Override
    public void delete(String tableName, String key) {
        this.put(tableName, key, DELETE_FLAG);
    }

    @Override
    public <T> T get(String tableName, String key) {
        return readLock(key, () -> {
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            Object value;
            if (puts != null && (value = puts.get(key)) != null) {
                if (value == DELETE_FLAG) {
                    return null;
                }
                return (T) value;
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            value = this.toObject(bytes);
            return (T) value;
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
        Map<String, Object> puts = this.tableNameToPuts.get(tableName);
        if (puts == null || puts.isEmpty()) {
            return;
        }
        long now = System.currentTimeMillis();
        long timestamp = tableNameToTimestamps.computeIfAbsent(tableName, s -> 0L);
        if (!force && puts.size() < this.batchSize && timestamp + interval > now) {
            return;
        }

        long totalBytes = 0;
        Map<byte[], byte[]> putBytes = new HashMap<>();
        Set<byte[]> deleteBytes = new HashSet<>();
        Consumer3<String, byte[], byte[]> putCallback = this.putCallbacks.get(tableName);
        Consumer2<String, byte[]> deleteCallback = this.deleteCallbacks.get(tableName);

        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, Object> entry : puts.entrySet()) {
            String key = entry.getKey();
            this.writeLock(key).lock();
            keys.add(key);
            puts.remove(key);

            byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
            totalBytes += bytesKey.length;
            Object value = entry.getValue();
            if (value == DELETE_FLAG) {
                deleteBytes.add(bytesKey);
                if (deleteCallback != null) {
                    deleteCallback.accept(key, bytesKey);
                }
                continue;
            }
            byte[] bytesValue = this.toBytes(value);
            totalBytes += bytesValue.length;
            putBytes.put(bytesKey, bytesValue);
            if (putCallback != null) {
                putCallback.accept(key, bytesKey, bytesValue);
            }
        }
        long elapsedTime = System.currentTimeMillis() - now;
        commit(tableName, putBytes, deleteBytes);
        for (String key : keys) {
            this.writeLock(key).unlock();
        }
        timestamp = System.currentTimeMillis();
        tableNameToTimestamps.put(tableName, timestamp);
        logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}]", path(), tableName, putBytes.size(), deleteBytes.size(), byteToString(totalBytes), elapsedTime, timestamp - now);
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
        this.tableNameToPuts.remove(tableName);
        this.tableNameToTimestamps.remove(tableName);
        return true;
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tableNames = new HashSet<>();
        tableNames.addAll(this.tableNameToPuts.keySet());
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
}

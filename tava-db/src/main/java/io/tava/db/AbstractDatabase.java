package io.tava.db;

import io.tava.function.Consumer0;
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
    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> tableNameToPuts = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tableNameToDeletes = new ConcurrentHashMap<>();
    private final int batchSize;
    private final int interval;
    private final boolean syncCheck;
    private final Serialization serialization;
    private final int initialCapacity;
    private long timestamp;

    protected AbstractDatabase(Serialization serialization, int batchSize, int interval, boolean syncCheck) {
        this.serialization = serialization;
        this.batchSize = batchSize;
        this.interval = interval;
        this.syncCheck = syncCheck;
        this.initialCapacity = this.batchSize / 2;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void put(String tableName, Map<String, Object> keyValues) {
        writeLock(tableName, () -> {
            this.tableNameToPuts.computeIfAbsent(tableName, s -> new HashMap<>(this.initialCapacity)).putAll(keyValues);
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).removeAll(keyValues.keySet());
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void put(String tableName, String key, Object value) {
        writeLock(tableName, () -> {
            this.tableNameToPuts.computeIfAbsent(tableName, s -> new HashMap<>(this.initialCapacity)).put(key, value);
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).remove(key);
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void delete(String tableName, Set<String> keys) {
        writeLock(tableName, () -> {
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                keys.forEach(puts::remove);
            }
            Set<String> deletes = this.tableNameToDeletes.computeIfAbsent(tableName, key -> new HashSet<>(this.initialCapacity));
            deletes.addAll(keys);
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void delete(String tableName, String key) {
        writeLock(tableName, () -> {
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                puts.remove(key);
            }
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).add(key);
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public Object get(String tableName, String key) {
        return get(tableName, key, true);
    }

    public Map<String, Object> get(String tableName, Set<String> keys) {
        Map<String, Object> values = readLock(tableName, () -> {
            Map<String, Object> map = new HashMap<>();
            for (String key : keys) {
                Set<String> deletes = this.tableNameToDeletes.get(tableName);
                if (deletes != null && deletes.contains(key)) {
                    map.put(key, null);
                    continue;
                }

                Map<String, Object> puts = this.tableNameToPuts.get(key);
                Object value;
                if (puts != null && (value = puts.get(key)) != null) {
                    map.put(key, value);
                }
            }
            return map;
        });

        keys.removeAll(values.keySet());

        List<byte[]> keyList = new ArrayList<>();
        for (String key : keys) {
            keyList.add(key.getBytes(StandardCharsets.UTF_8));
        }

        List<byte[]> bytes = get(tableName, keyList);
        for (int index = 0; index < keyList.size(); index++) {
            byte[] keyBytes = keyList.get(index);
            byte[] valueBytes = bytes.get(index);
            String key = new String(keyBytes, StandardCharsets.UTF_8);
            if (valueBytes == null || valueBytes.length == 0) {
                values.put(key, null);
                continue;
            }
            values.put(key, fromBytes(valueBytes));
        }

        return values;
    }

    @Override
    public Object get(String tableName, String key, boolean update) {
        return readLock(tableName, () -> {
            Set<String> deletes = this.tableNameToDeletes.get(tableName);
            if (deletes != null && deletes.contains(key)) {
                return null;
            }

            Map<String, Object> puts = this.tableNameToPuts.get(key);
            Object value;
            if (puts != null && (value = puts.get(key)) != null) {
                return value;
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            value = fromBytes(bytes);
            if (update) {
                this.tableNameToPuts.computeIfAbsent(tableName, s -> new HashMap<>(this.initialCapacity)).put(key, value);
            }
            return value;
        });
    }

    @Override
    public void commit(String tableName, boolean force) {
        this.writeLock(tableName, () -> {
            int size = 0;
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                size += puts.size();
            }
            Set<String> deletes = this.tableNameToDeletes.get(tableName);
            if (deletes != null) {
                size += deletes.size();
            }
            if (size == 0) {
                return;
            }
            long now = System.currentTimeMillis();
            if (!force && size < this.batchSize && this.timestamp + interval > now) {
                return;
            }
            long totalBytes = 0;
            Map<byte[], byte[]> putBytes = new HashMap<>();
            puts = this.tableNameToPuts.remove(tableName);
            if (puts != null) {
                for (Map.Entry<String, Object> entry : puts.entrySet()) {
                    byte[] key = entry.getKey().getBytes(StandardCharsets.UTF_8);
                    byte[] value = toBytes(entry.getValue());
                    totalBytes += key.length;
                    totalBytes += value.length;
                    putBytes.put(key, value);
                }
            }

            Set<byte[]> deleteBytes = new HashSet<>();
            deletes = this.tableNameToDeletes.remove(tableName);
            if (deletes != null) {
                for (String delete : deletes) {
                    byte[] key = delete.getBytes(StandardCharsets.UTF_8);
                    totalBytes += key.length;
                    deleteBytes.add(key);
                }
            }
            long elapsedTime = System.currentTimeMillis() - now;
            commit(tableName, putBytes, deleteBytes);
            this.timestamp = System.currentTimeMillis();
            logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}]", path(), tableName, putBytes.size(), deleteBytes.size(), byteToString(totalBytes), elapsedTime, this.timestamp - now);
        });
    }

    protected abstract List<byte[]> get(String tableName, List<byte[]> keys);

    protected abstract byte[] get(String tableName, byte[] key);

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes);

    @Override
    public Lock writeLock(String tableName) {
        return this.locks.computeIfAbsent(tableName, s -> new ReentrantReadWriteLock()).writeLock();
    }

    @Override
    public Lock readLock(String tableName) {
        return this.locks.computeIfAbsent(tableName, s -> new ReentrantReadWriteLock()).readLock();
    }

    protected <T> T readLock(String tableName, Function0<T> function) {
        try {
            this.readLock(tableName).lock();
            return function.apply();
        } finally {
            this.readLock(tableName).unlock();
        }
    }

    protected void writeLock(String tableName, Consumer0 consumer) {
        try {
            this.writeLock(tableName).lock();
            consumer.accept();
        } finally {
            this.writeLock(tableName).unlock();
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

    protected byte[] toBytes(Object value) {
        try {
            return this.serialization.toBytes(value);
        } catch (Exception cause) {
            this.logger.error("toBytes", cause);
            return null;
        }
    }

    protected Object fromBytes(byte[] bytes) {
        try {
            return this.serialization.fromBytes(bytes);
        } catch (Exception cause) {
            this.logger.error("toObject", cause);
            return null;
        }
    }

}

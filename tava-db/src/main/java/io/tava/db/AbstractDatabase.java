package io.tava.db;

import io.tava.function.Consumer0;
import io.tava.function.Function0;
import io.tava.serialization.KryoSerialization;
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
    private final Map<String, Map<String, Object>> puts = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> deletes = new ConcurrentHashMap<>();
    private final int batchSize;
    private final int interval;
    private final boolean syncCheck;
    private final KryoSerialization serialization;
    private long timestamp;

    protected AbstractDatabase(KryoSerialization serialization, int batchSize, int interval, boolean syncCheck) {
        this.serialization = serialization;
        this.batchSize = batchSize;
        this.interval = interval;
        this.syncCheck = syncCheck;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void put(String tableName, Map<String, Object> keyValues) {
        writeLock(tableName, () -> {
            this.puts.computeIfAbsent(tableName, s -> new HashMap<>()).putAll(keyValues);
            this.deletes.computeIfAbsent(tableName, s -> new HashSet<>()).removeAll(keyValues.keySet());
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void put(String tableName, String key, Object value) {
        writeLock(tableName, () -> {
            this.puts.computeIfAbsent(tableName, s -> new HashMap<>()).put(key, value);
            this.deletes.computeIfAbsent(tableName, s -> new HashSet<>()).remove(key);
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void delete(String tableName, Set<String> keys) {
        writeLock(tableName, () -> {
            Map<String, Object> map = this.puts.computeIfAbsent(tableName, s -> new HashMap<>());
            keys.forEach(map::remove);
            Set<String> set = this.deletes.computeIfAbsent(tableName, s -> new HashSet<>());
            set.addAll(keys);
        });
        if (!syncCheck) {
            return;
        }
        commit(tableName, false);
    }

    @Override
    public void delete(String tableName, String key) {
        writeLock(tableName, () -> {
            this.puts.computeIfAbsent(tableName, s -> new HashMap<>()).remove(key);
            this.deletes.computeIfAbsent(tableName, s -> new HashSet<>()).add(key);
        });
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
                if (this.deletes.computeIfAbsent(tableName, s -> new HashSet<>()).contains(key)) {
                    map.put(key, null);
                    continue;
                }
                Object value = this.puts.get(key);
                if (value != null) {
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
            values.put(key, toObject(valueBytes));
        }

        return values;
    }

    @Override
    public Object get(String tableName, String key, boolean update) {
        return readLock(tableName, () -> {
            if (this.deletes.computeIfAbsent(tableName, s -> new HashSet<>()).contains(key)) {
                return null;
            }
            Object value = this.puts.get(key);
            if (value != null) {
                return value;
            }
            byte[] bytes = this.get(tableName, toBytes(key));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            value = toObject(bytes);
            if (update) {
                this.puts.computeIfAbsent(tableName, s -> new HashMap<>()).put(key, value);
            }
            return value;
        });
    }

    @Override
    public void commit(String tableName, boolean force) {
        this.writeLock(tableName, () -> {
            int size = 0;

            Map<String, Object> map = this.puts.get(tableName);
            if (map != null) {
                size += map.size();
            }
            Set<String> set = this.deletes.get(tableName);
            if (set != null) {
                size += set.size();
            }
            if (size == 0) {
                return;
            }
            long now = System.currentTimeMillis();
            if (!force && size < this.batchSize && this.timestamp + interval > now) {
                return;
            }
            long totalBytes = 0;
            Map<byte[], byte[]> puts = new HashMap<>();
            map = this.puts.remove(tableName);
            if (map != null) {
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    byte[] key = toBytes(entry.getKey());
                    if (key == null) {
                        continue;
                    }
                    byte[] value = toBytes(entry.getValue());
                    if (value == null) {
                        continue;
                    }
                    totalBytes += key.length;
                    totalBytes += value.length;
                    puts.put(key, value);

                }
            }

            Set<byte[]> deletes = new HashSet<>();
            set = this.deletes.remove(tableName);
            if (set != null) {
                for (String delete : set) {
                    byte[] key = toBytes(delete);
                    totalBytes += key.length;
                    deletes.add(key);
                }
            }
            long elapsedTime = System.currentTimeMillis() - now;
            commit(tableName, puts, deletes);
            this.timestamp = System.currentTimeMillis();
            logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}]", path(), tableName, puts.size(), deletes.size(), byteToString(totalBytes), elapsedTime, this.timestamp - now);
            this.puts.clear();
            this.deletes.clear();
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

    protected Object toObject(byte[] bytes) {
        try {
            return serialization.fromBytes(bytes);
        } catch (Exception cause) {
            this.logger.error("toObject", cause);
            return null;
        }
    }

}

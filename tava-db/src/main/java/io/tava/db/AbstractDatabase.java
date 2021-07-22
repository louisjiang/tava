package io.tava.db;

import io.tava.db.util.Serialization;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
public abstract class AbstractDatabase implements Database {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Object> puts = new HashMap<>();
    private final Set<String> deletes = new HashSet<>();
    private final int batchSize;
    private final int interval;
    private final boolean syncCheck;
    private long timestamp;

    protected AbstractDatabase(int batchSize, int interval, boolean syncCheck) {
        this.batchSize = batchSize;
        this.interval = interval;
        this.syncCheck = syncCheck;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void put(Map<String, Object> keyValues) {
        writeLock(() -> {
            this.puts.putAll(keyValues);
            this.deletes.removeAll(keyValues.keySet());
        });
        if (!syncCheck) {
            return;
        }
        commit(false);
    }

    @Override
    public void put(String key, Object value) {
        writeLock(() -> {
            this.puts.put(key, value);
            this.deletes.remove(key);
        });
        if (!syncCheck) {
            return;
        }
        commit(false);
    }

    @Override
    public void delete(Set<String> keys) {
        writeLock(() -> {
            keys.forEach(this.puts::remove);
            this.deletes.addAll(keys);
        });
        if (!syncCheck) {
            return;
        }
        commit(false);
    }

    @Override
    public void delete(String key) {
        writeLock(() -> {
            this.puts.remove(key);
            this.deletes.add(key);
        });
        commit(false);
    }

    @Override
    public Object get(String key) {
        return get(key, true);
    }

    public Map<String, Object> get(Set<String> keys) {
        Map<String, Object> values = readLock(() -> {
            Map<String, Object> map = new HashMap<>();
            for (String key : keys) {
                if (this.deletes.contains(key)) {
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
            keyList.add(Serialization.toBytes(key));
        }

        List<byte[]> bytes = get(keyList);
        for (int index = 0; index < keyList.size(); index++) {
            byte[] keyBytes = keyList.get(index);
            byte[] valueBytes = bytes.get(index);
            if (valueBytes == null || valueBytes.length == 0) {
                values.put(Serialization.toString(keyBytes), null);
                continue;
            }
            values.put(Serialization.toString(keyBytes), Serialization.toObject(valueBytes));
        }

        return values;
    }

    @Override
    public Object get(String key, boolean update) {
        return readLock(() -> {
            if (this.deletes.contains(key)) {
                return null;
            }
            Object value = this.puts.get(key);
            if (value != null) {
                return value;
            }
            byte[] bytes = this.get(Serialization.toBytes(key));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            value = Serialization.toObject(bytes);
            if (update) {
                this.puts.put(key, value);
            }
            return value;
        });
    }

    @Override
    public void commit(boolean force) {
        this.writeLock(() -> {
            int size = this.puts.size() + this.deletes.size();
            if (size == 0) {
                return;
            }
            long now = System.currentTimeMillis();
            if (!force && size < this.batchSize && this.timestamp + interval > now) {
                return;
            }
            long totalBytes = 0;
            Map<byte[], byte[]> puts = new HashMap<>();
            for (Map.Entry<String, Object> entry : this.puts.entrySet()) {
                byte[] key = Serialization.toBytes(entry.getKey());
                byte[] value = Serialization.toBytes(entry.getValue());
                totalBytes += key.length;
                totalBytes += value.length;
                puts.put(key, value);
            }

            Set<byte[]> deletes = new HashSet<>();
            for (String delete : this.deletes) {
                byte[] key = Serialization.toBytes(delete);
                totalBytes += key.length;
                deletes.add(key);
            }
            long elapsedTime = System.currentTimeMillis() - now;
            commit(puts, deletes);
            this.timestamp = System.currentTimeMillis();
            logger.info("commit data to db [{}][{}][{}][{}][{}][{}]", path(), this.puts.size(), this.deletes.size(), byteToString(totalBytes), elapsedTime, this.timestamp - now);
            this.puts.clear();
            this.deletes.clear();
        });

    }

    protected abstract List<byte[]> get(List<byte[]> keys);

    protected abstract byte[] get(byte[] key);

    protected abstract void commit(Map<byte[], byte[]> puts, Set<byte[]> deletes);

    @Override
    public Lock writeLock() {
        return this.lock.writeLock();
    }

    @Override
    public Lock readLock() {
        return this.lock.readLock();
    }

    protected <T> T readLock(Function0<T> function) {
        this.readLock().lock();
        T value = function.apply();
        this.readLock().unlock();
        return value;
    }

    protected void writeLock(Consumer0 consumer) {
        this.writeLock().lock();
        consumer.accept();
        this.writeLock().unlock();
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
}

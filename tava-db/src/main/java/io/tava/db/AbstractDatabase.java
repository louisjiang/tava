package io.tava.db;

import io.tava.db.util.Serialization;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
    private long timestamp;

    protected AbstractDatabase(int batchSize, int interval) {
        this.batchSize = batchSize;
        this.interval = interval;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public void put(Map<String, Object> keyValues) {
        writeLock(() -> {
            this.puts.putAll(keyValues);
            this.deletes.removeAll(keyValues.keySet());
        });
        commit(false);
    }

    @Override
    public void put(String key, Object value) {
        writeLock(() -> {
            this.puts.put(key, value);
            this.deletes.remove(key);
        });
        commit(false);
    }

    @Override
    public void delete(Set<String> keys) {
        writeLock(() -> {
            keys.forEach(this.puts::remove);
            this.deletes.addAll(keys);
        });
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
            commit(puts, deletes);
            this.timestamp = System.currentTimeMillis();
            logger.info("commit data to db [{}][{}][{}][{}]", path(), this.puts.size(), this.deletes.size(), this.timestamp - now);
            this.puts.clear();
            this.deletes.clear();
        });

    }

    protected abstract byte[] get(byte[] key);

    protected abstract void commit(Map<String, Object> puts, Set<String> deletes);

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

}

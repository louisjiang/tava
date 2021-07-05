package io.tava.db;

import io.tava.db.util.Serialization;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.iq80.leveldb.WriteBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:38
 */
public class LeveldbDatabase implements Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeveldbDatabase.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Object> puts = new HashMap<>();
    private final Set<String> deletes = new HashSet<>();
    private final File path;
    private final int batchSize;
    private final int interval;
    private final long updateTimestamp = System.currentTimeMillis();
    private DB db;
    private boolean opened;

    public LeveldbDatabase(String path) {
        this(path, 128, 10000);
    }

    public LeveldbDatabase(String path, int batchSize, int interval) {
        this.path = new File(path);
        this.batchSize = batchSize;
        this.interval = interval;
        this.path.mkdirs();
        open(createOptions());
    }

    @Override
    public void put(String key, Object value) {
        writeLock(() -> {
            this.puts.put(key, value);
            this.deletes.remove(key);
        });
        commit0();
    }

    @Override
    public void put(Map<String, Object> keyValues) {
        writeLock(() -> {
            this.puts.putAll(keyValues);
            this.deletes.removeAll(keyValues.keySet());
        });
        commit0();
    }

    @Override
    public void delete(String key) {
        writeLock(() -> {
            this.puts.remove(key);
            this.deletes.add(key);
        });
        commit0();
    }

    @Override
    public void delete(Set<String> keys) {
        writeLock(() -> {
            for (String key : keys) {
                this.puts.remove(key);
            }
            this.deletes.addAll(keys);
        });
    }

    @Override
    public Object get(String key) {
        return readLock(() -> {
            if (this.deletes.contains(key)) {
                return null;
            }
            Object value = this.puts.get(key);
            if (value != null) {
                return value;
            }
            return Serialization.toObject(this.db.get(Serialization.toBytes(key)));
        });
    }


    private void commit0() {
        int size = this.puts.size() + this.deletes.size();
        if (size < this.batchSize || updateTimestamp + interval < System.currentTimeMillis()) {
            return;
        }
        commit();
    }

    @Override
    public void commit() {
        this.lock.readLock().lock();
        WriteBatch writeBatch = this.db.createWriteBatch();
        for (Map.Entry<String, Object> entry : this.puts.entrySet()) {
            writeBatch.put(Serialization.toBytes(entry.getKey()), Serialization.toBytes(entry.getValue()));
        }
        for (String delete : this.deletes) {
            writeBatch.delete(Serialization.toBytes(delete));
        }
        try {
            writeBatch.close();
        } catch (IOException cause) {
            LOGGER.error("close write batch", cause);
        }
        this.lock.readLock().unlock();
    }

    @Override
    public String path() {
        return path.getAbsolutePath();
    }

    @Override
    public void close() {
        if (this.opened) {
            try {
                this.db.close();
                this.opened = false;
            } catch (IOException e) {
                LOGGER.error("Failed to close database: {}", path, e);
            }
        }
    }

    private Options createOptions() {
        Options options = new Options();
        options.createIfMissing(true);
        options.compressionType(CompressionType.SNAPPY);
        options.blockSize(4 * 1024 * 1024);
        options.writeBufferSize(8 * 1024 * 1024);
        options.cacheSize(64L * 1024L * 1024L);
        options.paranoidChecks(true);
        options.verifyChecksums(true);
        options.maxOpenFiles(128);

        return options;
    }

    private void open(Options options) {
        try {
            db = JniDBFactory.factory.open(path, options);
            opened = true;
        } catch (IOException e) {
            if (e.getMessage().contains("Corruption")) {
                recover(options);

                try {
                    db = JniDBFactory.factory.open(path, options);
                    opened = true;
                } catch (IOException ex) {
                    LOGGER.error("Failed to open database", e);
                }
            }
        }
    }

    private void recover(Options options) {
        try {
            LOGGER.info("Trying to repair the database: {}", path);
            factory.repair(path, options);
            LOGGER.info("Repair done!");
        } catch (IOException cause) {
            LOGGER.error("Failed to repair the database", cause);
        }
    }

    private <T> T readLock(Function0<T> function) {
        this.lock.readLock().lock();
        T value = function.apply();
        this.lock.readLock().unlock();
        return value;
    }

    private void writeLock(Consumer0 consumer) {
        this.lock.writeLock().lock();
        consumer.accept();
        this.lock.writeLock().unlock();
    }

}

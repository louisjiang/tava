package io.tava.db;

import io.tava.db.util.Serialization;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:38
 */
public class LeveldbDatabase implements Database, org.iq80.leveldb.Logger {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeveldbDatabase.class);
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<String, Object> puts = new HashMap<>();
    private final Set<String> deletes = new HashSet<>();
    private final int batchSize;
    private final int interval;
    private final File directory;
    private long updateTimestamp = System.currentTimeMillis();
    private DB db;
    private boolean opened;

    public LeveldbDatabase(String path) {
        this(path, 1024, 10000);
    }

    public LeveldbDatabase(String path, int batchSize, int interval) {
        this.directory = new File(path);
        this.batchSize = batchSize;
        this.interval = interval;
        this.directory.mkdirs();
        open(createOptions());
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
    public void put(Map<String, Object> keyValues) {
        writeLock(() -> {
            this.puts.putAll(keyValues);
            this.deletes.removeAll(keyValues.keySet());
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
        return get(key, false);
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
            byte[] bytes = this.db.get(Serialization.toBytes(key));
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
    public Iterator iterator() {
        final DBIterator iterator = db.iterator();
        this.readLock().lock();
        iterator.seekToFirst();
        return new Iterator() {

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Entry next() {
                Map.Entry<byte[], byte[]> next = iterator.next();
                return new Entry(next.getKey(), next.getValue());
            }

            @Override
            public void close() throws IOException {
                readLock().unlock();
                iterator.close();
            }

        };
    }

    @Override
    public void commit(boolean force) {
        this.writeLock().lock();
        int size = this.puts.size() + this.deletes.size();
        if (size == 0) {
            this.writeLock().unlock();
            return;
        }
        if (!force && size < this.batchSize && this.updateTimestamp + interval > System.currentTimeMillis()) {
            this.writeLock().unlock();
            return;
        }
        WriteBatch writeBatch = this.db.createWriteBatch();
        for (Map.Entry<String, Object> entry : this.puts.entrySet()) {
            writeBatch.put(Serialization.toBytes(entry.getKey()), Serialization.toBytes(entry.getValue()));
        }
        for (String delete : this.deletes) {
            writeBatch.delete(Serialization.toBytes(delete));
        }
        try {
            LOGGER.info("commit data to leveldb[{}][{}][{}]", this.puts.size(), this.deletes.size(), directory.getPath());
            this.db.write(writeBatch, new WriteOptions().sync(true));
            writeBatch.close();
            this.puts.clear();
            this.deletes.clear();
            this.updateTimestamp = System.currentTimeMillis();
        } catch (IOException cause) {
            LOGGER.error("write batch", cause);
        }
        this.writeLock().unlock();
    }

    @Override
    public String path() {
        return directory.getPath();
    }

    @Override
    public Lock writeLock() {
        return this.lock.writeLock();
    }

    @Override
    public Lock readLock() {
        return this.lock.readLock();
    }

    @Override
    public void close() {
        if (this.opened) {
            try {
                this.db.close();
                this.opened = false;
            } catch (IOException e) {
                LOGGER.error("Failed to close database: {}", directory, e);
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
        options.maxOpenFiles(1024);
        options.logger(this);

        return options;
    }

    private void open(Options options) {
        try {
            db = JniDBFactory.factory.open(directory, options);
            opened = true;
        } catch (IOException e) {
            if (e.getMessage().contains("Corruption")) {
                recover(options);

                try {
                    db = JniDBFactory.factory.open(directory, options);
                    opened = true;
                } catch (IOException ex) {
                    LOGGER.error("Failed to open database", e);
                }
            }
        }
    }

    private void recover(Options options) {
        try {
            LOGGER.info("Trying to repair the database: {}", directory);
            factory.repair(directory, options);
            LOGGER.info("Repair done!");
        } catch (IOException cause) {
            LOGGER.error("Failed to repair the database", cause);
        }
    }

    private <T> T readLock(Function0<T> function) {
        this.readLock().lock();
        T value = function.apply();
        this.readLock().unlock();
        return value;
    }

    private void writeLock(Consumer0 consumer) {
        this.writeLock().lock();
        consumer.accept();
        this.writeLock().unlock();
    }

    @Override
    public void log(String message) {
        LOGGER.info(message);
    }
}

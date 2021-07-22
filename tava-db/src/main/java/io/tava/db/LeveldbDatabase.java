package io.tava.db;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:38
 */
public class LeveldbDatabase extends AbstractDatabase {

    private final File directory;
    private DB db;
    private boolean opened;

    public LeveldbDatabase(String path) {
        this(path, false);
    }

    public LeveldbDatabase(String path, boolean syncCheck) {
        this(path, 4096, 30000, syncCheck);
    }

    public LeveldbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck) {
        this(path, batchSize, interval, syncCheck, 128, 128, 128);
    }

    public LeveldbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck,
                           int blockSize,
                           int writeBufferSize,
                           long cacheSize) {
        super(batchSize, interval, syncCheck);
        this.directory = new File(path);
        this.directory.mkdirs();
        open(createOptions(blockSize, writeBufferSize, cacheSize));
    }

    @Override
    protected byte[] get(byte[] key) {
        return this.db.get(key);
    }

    @Override
    protected List<byte[]> get(List<byte[]> keys) {
        return null;
    }

    @Override
    protected void commit(Map<byte[], byte[]> puts, Set<byte[]> deletes) {
        WriteBatch writeBatch = this.db.createWriteBatch();
        for (Map.Entry<byte[], byte[]> entry : puts.entrySet()) {
            writeBatch.put(entry.getKey(), entry.getValue());
        }
        for (byte[] delete : deletes) {
            writeBatch.delete(delete);
        }
        try {
            this.db.write(writeBatch, new WriteOptions().sync(false));
            writeBatch.close();
        } catch (IOException cause) {
            logger.error("write batch", cause);
        }
    }

    @Override
    public Iterator iterator() {
        this.readLock().lock();
        final DBIterator iterator = this.db.iterator();
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
    public String path() {
        return directory.getPath();
    }

    @Override
    public void close() {
        if (this.opened) {
            try {
                this.db.close();
                this.opened = false;
            } catch (IOException e) {
                logger.error("Failed to close database: {}", directory, e);
            }
        }
    }

    private Options createOptions(int blockSize, int writeBufferSize, long cacheSize) {
        Options options = new Options();
        options.createIfMissing(true);
        options.compressionType(CompressionType.SNAPPY);
        options.blockSize(blockSize * 1024 * 1024);
        options.writeBufferSize(writeBufferSize * 1024 * 1024);
        options.cacheSize(cacheSize * 1024 * 1024);
        options.paranoidChecks(true);
        options.verifyChecksums(true);
        options.maxOpenFiles(1024);
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
                    logger.error("Failed to open database", e);
                }
            }
        }
    }

    private void recover(Options options) {
        try {
            logger.info("Trying to repair the database: {}", directory);
            factory.repair(directory, options);
            logger.info("Repair done!");
        } catch (IOException cause) {
            logger.error("Failed to repair the database", cause);
        }
    }

}

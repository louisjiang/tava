package io.tava.db;

import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:38
 */
public class LeveldbDatabase implements Database<byte[], byte[]> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LeveldbDatabase.class);
    private final File path;
    private DB db;
    private boolean opened;

    public LeveldbDatabase(String path) {
        this.path = new File(path);
        this.path.mkdirs();
        open(createOptions());
    }

    @Override
    public void put(byte[] key, byte[] value) {
        this.db.put(key, value);
    }

    @Override
    public void delete(byte[] key) {
        this.db.delete(key);
    }

    @Override
    public byte[] get(byte[] key) {
        return this.db.get(key);
    }

    @Override
    public WriteBatch<byte[], byte[]> writeBatch() {
        return new AbstractWriteBatch<byte[], byte[]>() {
            @Override
            public void commit() {
                org.iq80.leveldb.WriteBatch writeBatch = LeveldbDatabase.this.db.createWriteBatch();
                for (Map.Entry<byte[], byte[]> entry : this.puts.entrySet()) {
                    writeBatch.put(entry.getKey(), entry.getValue());
                }
                for (byte[] delete : this.deletes) {
                    writeBatch.delete(delete);
                }
                try {
                    writeBatch.close();
                } catch (IOException cause) {
                    LOGGER.error("close write batch", cause);
                }
                this.puts.clear();
                this.deletes.clear();
            }
        };
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

    @Override
    public DatabaseType type() {
        return DatabaseType.LEVELDB;
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
}

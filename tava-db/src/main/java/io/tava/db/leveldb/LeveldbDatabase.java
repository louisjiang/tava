package io.tava.db.leveldb;

import io.tava.db.AbstractWriteBatch;
import io.tava.db.WriteBatch;
import io.tava.db.Database;
import org.fusesource.leveldbjni.JniDBFactory;
import org.iq80.leveldb.CompressionType;
import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Set;

import static org.fusesource.leveldbjni.JniDBFactory.factory;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:38
 */
public class LeveldbDatabase implements Database {

    private static final Logger logger = LoggerFactory.getLogger(LeveldbDatabase.class);
    private final File file;
    private DB db;
    private boolean opened;

    public LeveldbDatabase(String path) {
        this.file = new File(path);
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException cause) {
                logger.error("createDirectories:{}", path, cause);
            }
        }

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
    public WriteBatch writeBatch() {
        return new AbstractWriteBatch() {

            @Override
            public void commit() {
                org.iq80.leveldb.WriteBatch writeBatch = db.createWriteBatch();
                for (byte[] delete : this.deletes) {
                    writeBatch.delete(delete);
                }
                Set<Map.Entry<byte[], byte[]>> entries = this.puts.entrySet();
                for (Map.Entry<byte[], byte[]> entry : entries) {
                    writeBatch.put(entry.getKey(), entry.getValue());
                }
                db.write(writeBatch);
                try {
                    writeBatch.close();
                } catch (IOException cause) {
                    logger.error("Failed to close write batch", cause);
                }
                this.deletes.clear();
                this.puts.clear();
            }
        };
    }

    @Override
    public void close() {
        if (this.opened) {
            try {
                this.db.close();
                this.opened = false;
            } catch (IOException e) {
                logger.error("Failed to close database: {}", file, e);
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
            db = JniDBFactory.factory.open(file, options);
            opened = true;
        } catch (IOException e) {
            if (e.getMessage().contains("Corruption")) {
                recover(options);

                try {
                    db = JniDBFactory.factory.open(file, options);
                    opened = true;
                } catch (IOException ex) {
                    logger.error("Failed to open database", e);
                }
            }
        }
    }

    private void recover(Options options) {
        try {
            logger.info("Trying to repair the database: {}", file);
            factory.repair(file, options);
            logger.info("Repair done!");
        } catch (IOException cause) {
            logger.error("Failed to repair the database", cause);
        }
    }
}
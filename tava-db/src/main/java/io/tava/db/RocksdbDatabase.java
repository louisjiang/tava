package io.tava.db;

import io.tava.db.util.Serialization;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.rocksdb.CompressionType.LZ4_COMPRESSION;

/**
 * https://github.com/johnzeng/rocksdb-doc-cn
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-13 16:12
 */
public class RocksdbDatabase extends AbstractDatabase {

    private final File directory;
    private RocksDB db;

    public RocksdbDatabase(String path) {
        this(path, 4096, 30000);
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval) {
        this(path, batchSize, interval, 128, 128);
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           long blockSize,
                           long writeBufferSize) {
        this(path, batchSize, interval, createOptions(blockSize, writeBufferSize));
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           Options options) {
        super(batchSize, interval);
        this.directory = new File(path);
        this.directory.mkdirs();
        try {
            this.db = RocksDB.open(options, path);
            Env env = db.getEnv();
            env.setBackgroundThreads(1, Priority.LOW);
            env.setBackgroundThreads(Runtime.getRuntime().availableProcessors(), Priority.HIGH);
        } catch (RocksDBException cause) {
            logger.error("open RocksDB:[{}]", path, cause);
        }
    }


    private static Options createOptions(long blockSize,
                                         long writeBufferSize) {
        Options options = new Options();
        options.setWriteBufferSize(writeBufferSize * SizeUnit.MB);
        options.setCompressionType(LZ4_COMPRESSION);
        options.setAtomicFlush(true);
        options.setCreateIfMissing(true);
        options.setParanoidChecks(true);
        options.setMaxOpenFiles(1024);
        options.setMaxBackgroundJobs(Runtime.getRuntime().availableProcessors());

        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setBlockSize(blockSize * SizeUnit.MB);
        tableConfig.setFilterPolicy(new BloomFilter());

        options.setTableFormatConfig(tableConfig);
        return options;
    }


    @Override
    protected byte[] get(byte[] key) {
        try {
            return this.db.get(key);
        } catch (RocksDBException cause) {
            logger.info("get", cause);
            return null;
        }
    }

    @Override
    protected void commit(Map<String, Object> puts, Set<String> deletes) {
        WriteBatch writeBatch = new WriteBatch();
        try {
            for (Map.Entry<String, Object> entry : puts.entrySet()) {
                writeBatch.put(Serialization.toBytes(entry.getKey()), Serialization.toBytes(entry.getValue()));
            }
            for (String delete : deletes) {
                writeBatch.delete(Serialization.toBytes(delete));
            }
            WriteOptions writeOptions = new WriteOptions();
            this.db.write(writeOptions, writeBatch);
            close(writeBatch);
            close(writeOptions);
        } catch (RocksDBException cause) {
            logger.info("batch write", cause);
        }
    }

    @Override
    public Iterator iterator() {
        this.readLock().lock();
        ReadOptions readOptions = new ReadOptions();
        readOptions.setBackgroundPurgeOnIteratorCleanup(true);
        RocksIterator iterator = this.db.newIterator(readOptions);
        iterator.seekToFirst();
        return new Iterator() {
            @Override
            public void close() throws IOException {
                RocksdbDatabase.this.close(iterator);
                RocksdbDatabase.this.close(readOptions);
                readLock().unlock();
            }

            @Override
            public boolean hasNext() {
                return iterator.isValid();
            }

            @Override
            public Entry next() {
                byte[] key = iterator.key();
                byte[] value = iterator.value();
                iterator.next();
                return new Entry(key, value);
            }
        };
    }

    @Override
    public String path() {
        return this.directory.getPath();
    }

    @Override
    public void close() {
        db.close();
    }

    private void close(AbstractImmutableNativeReference reference) {
        if (reference.isOwningHandle()) {
            reference.close();
        }
    }

}

package io.tava.db;

import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        this(path, false);
    }

    public RocksdbDatabase(String path, boolean syncCheck) {
        this(path, 4096, 30000, syncCheck);
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck) {
        this(path, batchSize, interval, syncCheck, 16, 64, 128);
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck,
                           int blockSize,
                           int blockCacheSize,
                           int writeBufferSize) {
        this(path, batchSize, interval, syncCheck, createOptions(blockSize, blockCacheSize, writeBufferSize));
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck,
                           Options options) {
        super(batchSize, interval, syncCheck);
        this.directory = new File(path);
        this.directory.mkdirs();
        try {
            this.db = RocksDB.open(options, path);
        } catch (RocksDBException cause) {
            logger.error("open RocksDB:[{}]", path, cause);
        }
    }


    private static Options createOptions(int blockSize,
                                         int blockCacheSize,
                                         int writeBufferSize) {
        Options options = new Options();
        options.setWriteBufferSize(writeBufferSize * SizeUnit.MB);
//        options.setMaxWriteBufferNumber(5);
//        options.setMinWriteBufferNumberToMerge(2);
        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
        options.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
        options.setAtomicFlush(true);
        options.setCreateIfMissing(true);
        options.setParanoidChecks(true);
        options.setMaxOpenFiles(1024);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        options.setMaxBackgroundJobs(availableProcessors);
        options.setLevelCompactionDynamicLevelBytes(true);
        options.setBytesPerSync(SizeUnit.MB);
        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

        Env env = options.getEnv();
        env.setBackgroundThreads(availableProcessors / 2, Priority.LOW);
        env.setBackgroundThreads(availableProcessors / 2, Priority.HIGH);

        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setFilterPolicy(new BloomFilter(10, false));
        tableConfig.setBlockCache(new LRUCache(blockCacheSize * SizeUnit.MB));
        tableConfig.setBlockSize(blockSize * SizeUnit.KB);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);

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
    protected List<byte[]> get(List<byte[]> keys) {
        try {
            return this.db.multiGetAsList(keys);
        } catch (RocksDBException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void commit(Map<byte[], byte[]> puts, Set<byte[]> deletes) {
        WriteBatch writeBatch = new WriteBatch();
        try {
            for (Map.Entry<byte[], byte[]> entry : puts.entrySet()) {
                writeBatch.put(entry.getKey(), entry.getValue());
            }
            for (byte[] delete : deletes) {
                writeBatch.delete(delete);
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

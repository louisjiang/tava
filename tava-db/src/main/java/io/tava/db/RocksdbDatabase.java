package io.tava.db;

import io.tava.Tava;
import io.tava.lang.Tuple3;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://github.com/johnzeng/rocksdb-doc-cn
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-13 16:12
 */
public class RocksdbDatabase extends AbstractDatabase {

    private final Map<String, ColumnFamilyHandle> columnFamilyHandles = new ConcurrentHashMap<>();
    private final ColumnFamilyOptions columnFamilyOptions;
    private final File directory;
    private final RocksDB db;

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
        this(path, batchSize, interval, syncCheck, createOptions(path, blockSize, blockCacheSize, writeBufferSize));
    }

    public RocksdbDatabase(String path,
                           int batchSize,
                           int interval,
                           boolean syncCheck,
                           Tuple3<DBOptions, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> tuple3) {
        super(batchSize, interval, syncCheck);
        this.directory = new File(path);
        this.directory.mkdirs();
        this.columnFamilyOptions = tuple3.getValue2();
        try {
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            this.db = RocksDB.open(tuple3.getValue1(), path, tuple3.getValue3(), columnFamilyHandles);
            for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
                this.columnFamilyHandles.put(new String(columnFamilyHandle.getName(), StandardCharsets.UTF_8), columnFamilyHandle);
            }
        } catch (RocksDBException cause) {
            throw new RuntimeException("open RocksDB:" + path, cause);
        }
    }


    private static Tuple3<DBOptions, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> createOptions(String path,
                                                                                                      int blockSize,
                                                                                                      int blockCacheSize,
                                                                                                      int writeBufferSize) {
        DBOptions options = new DBOptions();
//        options.setWriteBufferSize(writeBufferSize * SizeUnit.MB);
//        options.setCompressionType(CompressionType.LZ4_COMPRESSION);
//        options.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
        options.setAtomicFlush(true);
        options.setCreateIfMissing(true);
        options.setParanoidChecks(true);
        options.setMaxOpenFiles(1024);
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        options.setMaxBackgroundJobs(availableProcessors);
//        options.setLevelCompactionDynamicLevelBytes(true);
        options.setBytesPerSync(SizeUnit.MB);
//        options.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

//        Env env = options.getEnv();
//        env.setBackgroundThreads(availableProcessors / 2, Priority.LOW);
//        env.setBackgroundThreads(availableProcessors / 2, Priority.HIGH);

        ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
        columnFamilyOptions.setWriteBufferSize(writeBufferSize * SizeUnit.MB);
        columnFamilyOptions.setCompressionType(CompressionType.LZ4_COMPRESSION);
        columnFamilyOptions.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
        columnFamilyOptions.setLevelCompactionDynamicLevelBytes(true);
        columnFamilyOptions.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setFilterPolicy(new BloomFilter(10, false));
        tableConfig.setBlockCache(new LRUCache(blockCacheSize * SizeUnit.MB));
        tableConfig.setBlockSize(blockSize * SizeUnit.KB);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);

        columnFamilyOptions.setTableFormatConfig(tableConfig);

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        try {
            List<byte[]> listColumnFamilies = RocksDB.listColumnFamilies(new Options(), path);
            if (listColumnFamilies.size() == 0) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor("default".getBytes(StandardCharsets.UTF_8), columnFamilyOptions));
            }
            for (byte[] columnFamily : listColumnFamilies) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamily, columnFamilyOptions));
            }
        } catch (RocksDBException cause) {
            throw new RuntimeException("listColumnFamilies", cause);
        }
        return Tava.of(options, columnFamilyOptions, columnFamilyDescriptors);
    }


    @Override
    protected byte[] get(String tableName, byte[] key) {
        try {
            return this.db.get(columnFamilyHandle(tableName), key);
        } catch (RocksDBException cause) {
            logger.info("get [{}]", tableName, cause);
            return null;
        }
    }

    @Override
    protected List<byte[]> get(String tableName, List<byte[]> keys) {
        try {
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            ColumnFamilyHandle columnFamilyHandle = columnFamilyHandle(tableName);
            for (int i = 0; i < keys.size(); i++) {
                columnFamilyHandles.add(columnFamilyHandle);
            }
            return this.db.multiGetAsList(columnFamilyHandles, keys);
        } catch (RocksDBException cause) {
            logger.error("get [{}]", tableName, cause);
            return null;
        }
    }

    @Override
    protected void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes) {
        WriteBatch writeBatch = new WriteBatch();
        try {
            ColumnFamilyHandle columnFamilyHandle = columnFamilyHandle(tableName);
            for (Map.Entry<byte[], byte[]> entry : puts.entrySet()) {
                writeBatch.put(columnFamilyHandle, entry.getKey(), entry.getValue());
            }
            for (byte[] delete : deletes) {
                writeBatch.delete(columnFamilyHandle, delete);
            }
            WriteOptions writeOptions = new WriteOptions();
            this.db.write(writeOptions, writeBatch);
            close(writeBatch);
            close(writeOptions);
        } catch (RocksDBException cause) {
            logger.info("commit", cause);
        }
    }

    @Override
    public Iterator iterator(String tableName) {
        this.readLock(tableName).lock();
        ReadOptions readOptions = new ReadOptions();
        readOptions.setBackgroundPurgeOnIteratorCleanup(true);
        RocksIterator iterator = this.db.newIterator(columnFamilyHandle(tableName), readOptions);
        iterator.seekToFirst();
        return new Iterator() {
            @Override
            public void close() throws IOException {
                RocksdbDatabase.this.close(iterator);
                RocksdbDatabase.this.close(readOptions);
                readLock(tableName).unlock();
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
    public boolean createTable(String tableName) {
        ColumnFamilyHandle columnFamilyHandle = this.columnFamilyHandles.get(tableName);
        if (columnFamilyHandle != null) {
            this.logger.warn("table:[{}] already exists", tableName);
            return false;
        }
        try {
            columnFamilyHandle(tableName);
            return true;
        } catch (Exception cause) {
            this.logger.error("createTable", cause);
            return false;
        }
    }

    @Override
    public boolean dropTable(String tableName) {
        ColumnFamilyHandle columnFamilyHandle = this.columnFamilyHandles.get(tableName);
        if (columnFamilyHandle == null) {
            this.logger.warn("table:[{}] does not exist", tableName);
            return false;
        }
        try {
            this.db.dropColumnFamily(columnFamilyHandle);
            return true;
        } catch (RocksDBException cause) {
            this.logger.error("dropTable", cause);
            return false;
        }
    }

    @Override
    public Set<String> getTableNames() {
        return new HashSet<>(this.columnFamilyHandles.keySet());
    }

    @Override
    public void close() {
        this.db.close();
    }

    private ColumnFamilyHandle columnFamilyHandle(String tableName) {
        return this.columnFamilyHandles.computeIfAbsent(tableName, key -> {
            try {
                return this.db.createColumnFamily(new ColumnFamilyDescriptor(tableName.getBytes(StandardCharsets.UTF_8), this.columnFamilyOptions));
            } catch (RocksDBException cause) {
                throw new RuntimeException("createColumnFamily", cause);
            }
        });
    }

    private void close(AbstractImmutableNativeReference reference) {
        if (reference.isOwningHandle()) {
            reference.close();
        }
    }

}

package io.tava.db;

import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.lang.Tuple2;
import io.tava.lang.Tuple3;
import io.tava.serialization.Serialization;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.rocksdb.*;
import org.rocksdb.util.SizeUnit;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * https://github.com/johnzeng/rocksdb-doc-cn
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-13 16:12
 */
public class RocksdbDatabase extends AbstractDatabase {

    private final Map<String, ColumnFamilyHandle> columnFamilyHandles = new ConcurrentHashMap<>();
    private final WriteOptions writeOptions = new WriteOptions();
    private final GenericObjectPool<WriteBatch> pool;
    private final ColumnFamilyOptions columnFamilyOptions;
    private final File directory;
    private final RocksDB db;

    public RocksdbDatabase(Configuration configuration, Serialization serialization) {
        this(configuration, serialization, createOptions(configuration));
    }

    public RocksdbDatabase(Configuration configuration,
                           Serialization serialization,
                           Tuple3<DBOptions, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> tuple3) {
        super(configuration, serialization);
        String path = configuration.getString("path");
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
        this.pool = new GenericObjectPool<>(new BasePooledObjectFactory<WriteBatch>() {
            @Override
            public WriteBatch create() throws Exception {
                return new WriteBatch();
            }

            @Override
            public void passivateObject(PooledObject<WriteBatch> p) throws Exception {
                p.getObject().clear();
            }

            @Override
            public void destroyObject(PooledObject<WriteBatch> p) throws Exception {
                p.getObject().close();
            }

            @Override
            public PooledObject<WriteBatch> wrap(WriteBatch writeBatch) {
                return new DefaultPooledObject<>(writeBatch);
            }
        }, createPoolConfig(configuration.getInt("maxTotal", 16), configuration.getInt("maxIdle", 16), configuration.getInt("minIdle", 1), configuration.getLong("maxWaitMilliseconds", -1)));
    }

    private GenericObjectPoolConfig<WriteBatch> createPoolConfig(int maxTotal,
                                                                 int maxIdle,
                                                                 int minIdle,
                                                                 long maxWaitMilliseconds) {
        GenericObjectPoolConfig<WriteBatch> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        if (maxWaitMilliseconds != -1) {
            poolConfig.setMaxWait(java.time.Duration.ofMillis(maxWaitMilliseconds));
        }
        return poolConfig;
    }

    private static Tuple3<DBOptions, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> createOptions(Configuration configuration) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        DBOptions options = new DBOptions();
        options.setAtomicFlush(true);
        options.setCreateIfMissing(true);
        options.setParanoidChecks(true);
        options.setMaxOpenFiles(configuration.getInt("max_open_files", 1024));
        options.setMaxBackgroundJobs(availableProcessors * 2);
        options.setBytesPerSync(32 * SizeUnit.MB);
        long writeBufferSize = configuration.getInt("write_buffer_size", 64) * SizeUnit.MB;
        LRUCache blockCache = new LRUCache(configuration.getInt("block_cache_size", 64) * SizeUnit.MB);
        options.setWriteBufferManager(new WriteBufferManager(writeBufferSize * 2, blockCache, true));
        Env env = Env.getDefault();
        env.setBackgroundThreads(availableProcessors * 2);
        options.setEnv(env);

        ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
        columnFamilyOptions.setWriteBufferSize(writeBufferSize);
        columnFamilyOptions.setMaxWriteBufferNumber(configuration.getInt("max_write_buffer_number", 2));
        columnFamilyOptions.setCompressionType(CompressionType.LZ4_COMPRESSION);
        columnFamilyOptions.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
//        columnFamilyOptions.setLevelCompactionDynamicLevelBytes(true);
        columnFamilyOptions.setCompactionPriority(CompactionPriority.MinOverlappingRatio);

        columnFamilyOptions.setMaxBytesForLevelBase(configuration.getInt("max_bytes_for_level_base", 64) * SizeUnit.MB);
        columnFamilyOptions.setMaxBytesForLevelMultiplier(configuration.getInt("max_bytes_for_level_multiplier", 10));
        columnFamilyOptions.setLevel0FileNumCompactionTrigger(5);
        columnFamilyOptions.setLevel0SlowdownWritesTrigger(20);
        columnFamilyOptions.setLevel0StopWritesTrigger(40);

        BlockBasedTableConfig tableConfig = new BlockBasedTableConfig();
        tableConfig.setIndexType(IndexType.kTwoLevelIndexSearch);
        tableConfig.setFilterPolicy(new BloomFilter(10, false));
        tableConfig.setPartitionFilters(true);
        tableConfig.setMetadataBlockSize(4096);
        tableConfig.setCacheIndexAndFilterBlocks(true);
        tableConfig.setPinTopLevelIndexAndFilter(true);
        tableConfig.setCacheIndexAndFilterBlocksWithHighPriority(true);
        tableConfig.setPinL0FilterAndIndexBlocksInCache(true);
        tableConfig.setBlockSize(configuration.getInt("block_size", 16) * SizeUnit.KB);
        tableConfig.setBlockCache(blockCache);

        columnFamilyOptions.setTableFormatConfig(tableConfig);

        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        try {
            List<byte[]> listColumnFamilies = RocksDB.listColumnFamilies(new Options(), configuration.getString("path"));
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
        if (puts == null && deletes == null) {
            return;
        }
        WriteBatch writeBatch = null;
        try {
            writeBatch = this.pool.borrowObject();
            ColumnFamilyHandle columnFamilyHandle = columnFamilyHandle(tableName);
            if (puts != null) {
                for (Map.Entry<byte[], byte[]> entry : puts.entrySet()) {
                    writeBatch.put(columnFamilyHandle, entry.getKey(), entry.getValue());
                }
            }
            if (deletes != null) {
                for (byte[] delete : deletes) {
                    writeBatch.delete(columnFamilyHandle, delete);
                }
            }
            this.db.write(writeOptions, writeBatch);
        } catch (Exception cause) {
            logger.info("commit", cause);
        } finally {
            if (writeBatch != null) {
                this.pool.returnObject(writeBatch);
            }
        }
    }

    private Tuple2<ReadOptions, Snapshot> newReadOptions(boolean useSnapshot) {
        ReadOptions readOptions = new ReadOptions();
        readOptions.setBackgroundPurgeOnIteratorCleanup(true);
        if (useSnapshot) {
            Snapshot snapshot = this.db.getSnapshot();
            readOptions.setSnapshot(snapshot);
            return Tava.of(readOptions, snapshot);
        }
        return Tava.of(readOptions, null);
    }

    @Override
    public Iterator iterator(String tableName, boolean useSnapshot) {
        Tuple2<ReadOptions, Snapshot> tuple2 = newReadOptions(useSnapshot);
        ReadOptions readOptions = tuple2.getValue1();
        Snapshot snapshot = tuple2.getValue2();
        if (snapshot == null) {
            this.readLock(tableName).lock();
        }

        RocksIterator iterator = this.db.newIterator(columnFamilyHandle(tableName), readOptions);
        iterator.seekToFirst();
        return new Iterator() {
            @Override
            public void close() throws IOException {
                RocksdbDatabase.this.close(iterator);
                RocksdbDatabase.this.close(readOptions);
                if (snapshot != null) {
                    db.releaseSnapshot(snapshot);
                    return;
                }
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
                return new Entry(key, value, RocksdbDatabase.this);
            }
        };
    }

    @Override
    public Tuple2<Boolean, byte[]> keyMayExist(String tableName, String key) {
        Holder<byte[]> holder = new Holder<>();
        boolean keyMayExist = this.db.keyMayExist(columnFamilyHandle(tableName), key.getBytes(StandardCharsets.UTF_8), holder);
        return Tava.of(keyMayExist, holder.getValue());
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
            this.logger.warn("drop table:[{}] does not exist", tableName);
            return false;
        }
        try {
            this.logger.info("drop table:{}", tableName);
            this.db.dropColumnFamily(columnFamilyHandle);
            this.columnFamilyHandles.remove(tableName);
            return super.dropTable(tableName);
        } catch (RocksDBException cause) {
            this.logger.error("drop table:[{}]", tableName, cause);
            return false;
        }
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tableNames = super.getTableNames();
        tableNames.addAll(this.columnFamilyHandles.keySet());
        return tableNames;
    }

    @Override
    public void compact(String tableName) {
        ColumnFamilyHandle columnFamilyHandle = this.columnFamilyHandles.get(tableName);
        if (columnFamilyHandle == null) {
            this.logger.warn("compact table:[{}] does not exist", tableName);
            return;
        }
        try {
            this.db.compactRange(columnFamilyHandle);
        } catch (RocksDBException cause) {
            this.logger.error("compact:[{}]", tableName, cause);
        }
    }

    @Override
    public void compact() {
        for (Map.Entry<String, ColumnFamilyHandle> entry : this.columnFamilyHandles.entrySet()) {
            try {
                this.db.compactRange(entry.getValue());
            } catch (RocksDBException cause) {
                this.logger.error("compact:[{}]", entry.getKey(), cause);
            }
        }
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

    private void close(AbstractNativeReference reference) {
        reference.close();
    }

}

package io.tava.db;

import com.alibaba.fastjson2.JSONObject;
import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.lang.Tuple2;
import io.tava.lang.Tuple5;
import io.tava.serialization.kryo.Serialization;
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
    private final WriteOptions writeOptions = new WriteOptions();
    private final ColumnFamilyOptions columnFamilyOptions;
    private final File directory;
    private final RocksDB db;
    private final Statistics statistics;
    private final Cache cache;

    public RocksdbDatabase(Configuration configuration, Serialization serialization) {
        this(configuration, serialization, createOptions(configuration));
    }

    public RocksdbDatabase(Configuration configuration, Serialization serialization, Tuple5<DBOptions, Statistics, Cache, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> tuple5) {
        super(configuration, serialization);
        String path = configuration.getString("path");
        this.directory = new File(path);
        this.directory.mkdirs();
        this.statistics = tuple5.getValue2();
        this.cache = tuple5.getValue3();
        this.columnFamilyOptions = tuple5.getValue4();
        try {
            List<ColumnFamilyHandle> columnFamilyHandles = new ArrayList<>();
            this.db = RocksDB.open(tuple5.getValue1(), path, tuple5.getValue5(), columnFamilyHandles);
            for (ColumnFamilyHandle columnFamilyHandle : columnFamilyHandles) {
                this.columnFamilyHandles.put(new String(columnFamilyHandle.getName(), StandardCharsets.UTF_8), columnFamilyHandle);
            }
        } catch (RocksDBException cause) {
            throw new RuntimeException("open RocksDB:" + path, cause);
        }
        this.writeOptions.setSync(false);
    }

    private static Tuple5<DBOptions, Statistics, Cache, ColumnFamilyOptions, List<ColumnFamilyDescriptor>> createOptions(Configuration configuration) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        DBOptions dbOptions = new DBOptions();
        dbOptions.setAtomicFlush(configuration.getBoolean("auto_flush", false));
        dbOptions.setCreateIfMissing(true);
        dbOptions.setParanoidChecks(true);
        dbOptions.setMaxOpenFiles(configuration.getInt("max_open_files", 1024));
        dbOptions.setMaxBackgroundJobs(availableProcessors * 2);
        dbOptions.setBytesPerSync(configuration.getInt("bytes_per_sync", 4) * SizeUnit.MB);
        dbOptions.setAllowMmapWrites(true);
        dbOptions.setAllowMmapReads(true);
        dbOptions.setEnablePipelinedWrite(configuration.getBoolean("enable_pipelined_write", true));
        dbOptions.setMaxTotalWalSize(configuration.getInt("max_total_wal_size", 1024) * SizeUnit.MB);
        dbOptions.setWalBytesPerSync(configuration.getLong("wal_bytes_per_sync", 4) * SizeUnit.MB);
        dbOptions.setMaxSubcompactions(configuration.getInt("max_sub_compactions", availableProcessors / 2));
        dbOptions.setInfoLogLevel(InfoLogLevel.valueOf(configuration.getString("info_log_level", "ERROR_LEVEL")));

        Statistics statistics = new Statistics();
        dbOptions.setStatistics(statistics);

        long writeBufferSize = configuration.getInt("db_write_buffer_size", 256) * SizeUnit.MB;
        int targetFileSize = configuration.getInt("target_file_size", 64);
        LRUCache blockCache = new LRUCache(configuration.getInt("block_cache_size", 64) * SizeUnit.MB, configuration.getInt("block_cache_num-shard-bits", 16), false);
        dbOptions.setWriteBufferManager(new WriteBufferManager(writeBufferSize, blockCache, true));
        Env env = Env.getDefault();
        env.setBackgroundThreads(availableProcessors, Priority.HIGH);
        env.setBackgroundThreads(availableProcessors / 2, Priority.LOW);
        dbOptions.setEnv(env);

        ColumnFamilyOptions columnFamilyOptions = new ColumnFamilyOptions();
        columnFamilyOptions.setWriteBufferSize(configuration.getInt("write_buffer_size", 64) * SizeUnit.MB);
        columnFamilyOptions.setMaxWriteBufferNumber(configuration.getInt("max_write_buffer_number", 5));
        columnFamilyOptions.setMinWriteBufferNumberToMerge(configuration.getInt("min_write_buffer_number_to_merge", 1));

        long periodicCompactionSeconds = configuration.getLong("periodic_compaction_seconds", 6 * 60 * 60);
        columnFamilyOptions.setTtl(periodicCompactionSeconds);
        columnFamilyOptions.setPeriodicCompactionSeconds(periodicCompactionSeconds);
        columnFamilyOptions.setCompressionType(CompressionType.LZ4_COMPRESSION);
        columnFamilyOptions.setTargetFileSizeBase(targetFileSize * SizeUnit.MB);
        CompressionOptions bottommostCompressionOptions = new CompressionOptions();
        bottommostCompressionOptions.setEnabled(true);
        bottommostCompressionOptions.setMaxDictBytes(112 * 1024);
        bottommostCompressionOptions.setZStdMaxTrainBytes(20 * 1024 * 1024);
        columnFamilyOptions.setBottommostCompressionOptions(bottommostCompressionOptions);
        columnFamilyOptions.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);

        columnFamilyOptions.setCompactionPriority(CompactionPriority.MinOverlappingRatio);
        columnFamilyOptions.setLevelCompactionDynamicLevelBytes(true);
        columnFamilyOptions.setCompactionStyle(CompactionStyle.LEVEL);

        columnFamilyOptions.setNumLevels(7);
        columnFamilyOptions.setMaxBytesForLevelBase(configuration.getInt("max_bytes_for_level_base", targetFileSize * 10) * SizeUnit.MB);
        columnFamilyOptions.setMaxBytesForLevelMultiplier(configuration.getInt("max_bytes_for_level_multiplier", 10));
        columnFamilyOptions.setLevel0FileNumCompactionTrigger(4);
        columnFamilyOptions.setLevel0SlowdownWritesTrigger(16);
        columnFamilyOptions.setLevel0StopWritesTrigger(32);

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

        if (configuration.getBoolean("blob_enable", true)) {
            columnFamilyOptions.setEnableBlobFiles(true);
            columnFamilyOptions.setBlobFileSize(configuration.getLong("blob_file_size", 256) * SizeUnit.MB);
            columnFamilyOptions.setMinBlobSize(configuration.getInt("min_blob_size", 4) * SizeUnit.KB);
            columnFamilyOptions.setBlobCompressionType(CompressionType.ZSTD_COMPRESSION);
            columnFamilyOptions.setBlobGarbageCollectionAgeCutoff(configuration.getDouble("blob_garbage_collection_age_cutoff", 0.5));
            columnFamilyOptions.setBlobGarbageCollectionForceThreshold(configuration.getDouble("blob_garbage_collection_force_threshold", 0.5));
            columnFamilyOptions.setEnableBlobGarbageCollection(true);
        }


        List<ColumnFamilyDescriptor> columnFamilyDescriptors = new ArrayList<>();
        try {
            List<byte[]> listColumnFamilies = RocksDB.listColumnFamilies(new Options(), configuration.getString("path"));
            if (listColumnFamilies.isEmpty()) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor("default".getBytes(StandardCharsets.UTF_8), columnFamilyOptions));
            }
            for (byte[] columnFamily : listColumnFamilies) {
                columnFamilyDescriptors.add(new ColumnFamilyDescriptor(columnFamily, columnFamilyOptions));
            }
        } catch (RocksDBException cause) {
            throw new RuntimeException("listColumnFamilies", cause);
        }
        return Tava.of(dbOptions, statistics, blockCache, columnFamilyOptions, columnFamilyDescriptors);
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
    protected void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes, int totalBytes) {
        if (puts.isEmpty() && deletes.isEmpty()) {
            return;
        }
        try (WriteBatch writeBatch = new WriteBatch(totalBytes)) {
            ColumnFamilyHandle columnFamilyHandle = this.columnFamilyHandle(tableName);
            for (Map.Entry<byte[], byte[]> entry : puts.entrySet()) {
                writeBatch.put(columnFamilyHandle, entry.getKey(), entry.getValue());
            }
            for (byte[] delete : deletes) {
                writeBatch.delete(columnFamilyHandle, delete);
            }
            this.db.write(writeOptions, writeBatch);
        } catch (Exception cause) {
            logger.error("commit", cause);
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
        RocksIterator iterator = this.db.newIterator(columnFamilyHandle(tableName), readOptions);
        iterator.seekToFirst();
        return new Iterator() {

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

            @Override
            public void close() throws IOException {
                RocksdbDatabase.this.close(iterator);
                RocksdbDatabase.this.close(readOptions);
                if (snapshot != null) {
                    db.releaseSnapshot(snapshot);
                }
            }
        };
    }

    @Override
    public boolean keyMayExist(String tableName, String key) {
        Map<String, Operation> operationMap = this.tableNameToOperationMap.get(tableName);
        if (nonEmpty(operationMap)) {
            Operation operation = operationMap.get(key);
            if (nonEmpty(operation)) {
                return !operation.isDelete();
            }
        }
        return this.db.keyMayExist(columnFamilyHandle(tableName), key.getBytes(StandardCharsets.UTF_8), null);
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
            this.columnFamilyHandle(tableName);
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
            columnFamilyHandle.close();
            return super.dropTable(tableName);
        } catch (RocksDBException cause) {
            this.logger.error("drop table:[{}]", tableName, cause);
            return false;
        }
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tableNames = new HashSet<>();
        tableNames.addAll(this.tableNameToOperationMap.keySet());
        tableNames.addAll(this.columnFamilyHandles.keySet());
        return tableNames;
    }

    @Override
    public boolean hasTable(String tableName) {
        if (this.tableNameToOperationMap.containsKey(tableName)) {
            return true;
        }
        return this.columnFamilyHandles.containsKey(tableName);
    }

    @Override
    public void compactRange(String tableName) {
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
    public void compactRange() {
        for (Map.Entry<String, ColumnFamilyHandle> entry : this.columnFamilyHandles.entrySet()) {
            compactRange(entry.getKey());
        }
    }

    @Override
    public int flush() {
        FlushOptions flushOptions = new FlushOptions();
        flushOptions.setWaitForFlush(true);
        int count = 0;
        for (Map.Entry<String, ColumnFamilyHandle> entry : this.columnFamilyHandles.entrySet()) {
            try {
                this.db.flush(flushOptions, entry.getValue());
                count++;
            } catch (RocksDBException ignored) {
            }
        }
        return count;
    }

    @Override
    public JSONObject metaData() {
        JSONObject metaData = new JSONObject();
        long totalSize = 0;
        long totalFileCount = 0;
        for (Map.Entry<String, ColumnFamilyHandle> entry : columnFamilyHandles.entrySet()) {
            ColumnFamilyMetaData columnFamilyMetaData = db.getColumnFamilyMetaData(entry.getValue());
            JSONObject jsonObject = new JSONObject();
            long size = columnFamilyMetaData.size();
            long fileCount = columnFamilyMetaData.fileCount();
            jsonObject.put("size", byteToString(size));
            jsonObject.put("fileCount", fileCount);
            metaData.put(entry.getKey(), jsonObject);
            totalSize += size;
            totalFileCount += fileCount;
        }

        metaData.put("totalSize", byteToString(totalSize));
        metaData.put("totalFileCount", totalFileCount);

        return metaData;
    }


    @Override
    public JSONObject statistics() {
        JSONObject jsonObject = new JSONObject();
        JSONObject tickers = new JSONObject();

        for (TickerType tickerType : TickerType.values()) {
            long tickerCount = this.statistics.getTickerCount(tickerType);
            tickers.put(tickerType.name(), tickerCount);
        }

        JSONObject histograms = new JSONObject();
        for (HistogramType histogramType : HistogramType.values()) {
            String histogramString = this.statistics.getHistogramString(histogramType);
            histograms.put(histogramType.name(), histogramString);
        }

        jsonObject.put("tickers", tickers);
        jsonObject.put("histograms", histograms);

        jsonObject.put("pinnedUsage", cache.getPinnedUsage());
        jsonObject.put("usage", cache.getUsage());

        return jsonObject;
    }

    @Override
    public void close() {
        close(this.db);
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

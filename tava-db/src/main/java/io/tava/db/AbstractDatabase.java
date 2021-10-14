package io.tava.db;

import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import io.tava.configuration.Configuration;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import io.tava.serialization.Serialization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-14 13:31
 */
public abstract class AbstractDatabase implements Database {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final byte[] BLOOM_FILTER_KEY = "BLOOM_FILTER".getBytes(StandardCharsets.UTF_8);
    private final Map<String, ReadWriteLock> locks = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> tableNameToPuts = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tableNameToDeletes = new ConcurrentHashMap<>();
    private final Map<String, Long> tableNameToTimestamps = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> tableNameToResidentMemories = new ConcurrentHashMap<>();
    private final Map<String, BloomFilter<String>> tableNameToBloomFilters = new ConcurrentHashMap<>();
    private final Serialization serialization;
    private final int batchSize;
    private final int interval;
    private final int initialCapacity;
    private final boolean bloomFilterEnabled;
    private final int expectedInsertions;
    private final double fpp;

    protected AbstractDatabase(Configuration configuration, Serialization serialization) {
        this.serialization = serialization;
        this.batchSize = configuration.getInt("batch_size", 2048);
        this.interval = configuration.getInt("interval", 10000);
        this.initialCapacity = this.batchSize / 2;
        this.bloomFilterEnabled = configuration.getBoolean("bloom_filter.enabled", true);
        this.expectedInsertions = configuration.getInt("bloom_filter.expected_insertions", 1000000);
        this.fpp = configuration.getDouble("bloom_filter.fpp", 0.03D);
    }

    @Override
    public void put(String tableName, Map<String, Object> keyValues, boolean residentMemory) {
        writeLock(tableName, () -> {
            this.tableNameToPuts.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).putAll(keyValues);
            Set<String> keys = keyValues.keySet();
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).removeAll(keys);
            if (bloomFilterEnabled) {
                BloomFilter<String> bloomFilter = getBloomFilter(tableName);
                keys.forEach(bloomFilter::put);
            }
            if (residentMemory) {
                this.tableNameToResidentMemories.computeIfAbsent(tableName, s -> new HashSet<>()).addAll(keys);
            }
        });
    }

    @Override
    public void put(String tableName, String key, Object value, boolean residentMemory) {
        writeLock(tableName, () -> {
            this.tableNameToPuts.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).put(key, value);
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).remove(key);
            if (bloomFilterEnabled) {
                getBloomFilter(tableName).put(key);
            }
            if (residentMemory) {
                this.tableNameToResidentMemories.computeIfAbsent(tableName, s -> new HashSet<>()).add(key);
            }
        });
    }

    @Override
    public void delete(String tableName, Set<String> keys) {
        writeLock(tableName, () -> {
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                keys.forEach(puts::remove);
            }
            this.tableNameToResidentMemories.computeIfAbsent(tableName, s -> new HashSet<>()).removeAll(keys);
            Set<String> deletes = this.tableNameToDeletes.computeIfAbsent(tableName, key -> new HashSet<>(this.initialCapacity));
            deletes.addAll(keys);
        });
    }

    @Override
    public void delete(String tableName, String key) {
        writeLock(tableName, () -> {
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                puts.remove(key);
            }
            this.tableNameToResidentMemories.computeIfAbsent(tableName, s -> new HashSet<>()).remove(key);
            this.tableNameToDeletes.computeIfAbsent(tableName, s -> new HashSet<>(this.initialCapacity)).add(key);
        });
    }

    @Override
    public Object get(String tableName, String key) {
        return get(tableName, key, true);
    }

    public Map<String, Object> get(String tableName, Set<String> keys) {
        return readLock(tableName, () -> {
            Map<String, Object> values = new HashMap<>();
            for (String key : keys) {
                Set<String> deletes = this.tableNameToDeletes.get(tableName);
                if (deletes != null && deletes.contains(key)) {
                    values.put(key, null);
                    continue;
                }

                Map<String, Object> puts = this.tableNameToPuts.get(key);
                Object value;
                if (puts != null && (value = puts.get(key)) != null) {
                    values.put(key, value);
                }
            }

            keys.removeAll(values.keySet());
            List<byte[]> keyList = new ArrayList<>();
            for (String key : keys) {
                keyList.add(key.getBytes(StandardCharsets.UTF_8));
            }

            List<byte[]> bytes = get(tableName, keyList);
            for (int index = 0; index < keyList.size(); index++) {
                byte[] keyBytes = keyList.get(index);
                byte[] valueBytes = bytes.get(index);
                String key = new String(keyBytes, StandardCharsets.UTF_8);
                if (valueBytes == null || valueBytes.length == 0) {
                    values.put(key, null);
                    continue;
                }
                values.put(key, fromBytes(valueBytes));
            }

            return values;
        });
    }

    @Override
    public Object get(String tableName, String key, boolean forUpdate) {
        return readLock(tableName, () -> {
            Set<String> deletes = this.tableNameToDeletes.get(tableName);
            if (deletes != null && deletes.contains(key)) {
                return null;
            }

            Map<String, Object> puts = this.tableNameToPuts.get(key);
            Object value;
            if (puts != null && (value = puts.get(key)) != null) {
                return value;
            }
            byte[] bytes = this.get(tableName, key.getBytes(StandardCharsets.UTF_8));
            if (bytes == null || bytes.length == 0) {
                return null;
            }
            value = fromBytes(bytes);
            if (forUpdate) {
                this.tableNameToPuts.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).put(key, value);
            }
            return value;
        });
    }

    @Override
    public void tryCommit(String tableName) {
        commit(tableName, false);
    }

    @Override
    public void commit(String tableName) {
        commit(tableName, true);
    }

    private void commit(String tableName, boolean force) {
        this.writeLock(tableName, () -> {
            int size = 0;
            Map<String, Object> puts = this.tableNameToPuts.get(tableName);
            if (puts != null) {
                size += puts.size();
            }
            Set<String> deletes = this.tableNameToDeletes.get(tableName);
            if (deletes != null) {
                size += deletes.size();
            }
            if (size == 0) {
                return;
            }
            long now = System.currentTimeMillis();
            long timestamp = tableNameToTimestamps.computeIfAbsent(tableName, s -> 0L);
            if (!force && size < this.batchSize && timestamp + interval > now) {
                return;
            }
            long totalBytes = 0;
            Map<byte[], byte[]> putBytes = null;
            puts = this.tableNameToPuts.remove(tableName);
            if (puts != null) {
                putBytes = new HashMap<>(puts.size());
                Set<String> residentMemories = this.tableNameToResidentMemories.get(tableName);
                for (Map.Entry<String, Object> entry : puts.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (residentMemories != null && residentMemories.contains(key)) {
                        this.logger.info("resident memory key:[{}]", key);
                        this.tableNameToPuts.computeIfAbsent(tableName, s -> new ConcurrentHashMap<>(this.initialCapacity)).put(key, value);
                    }
                    byte[] bytesKey = key.getBytes(StandardCharsets.UTF_8);
                    byte[] bytesValue = toBytes(value);
                    totalBytes += bytesKey.length;
                    totalBytes += bytesValue.length;
                    putBytes.put(bytesKey, bytesValue);
                }
                byte[] bytes = toBytes(this.tableNameToBloomFilters.get(tableName));
                if (bytes != null) {
                    totalBytes += bytes.length;
                    putBytes.put(BLOOM_FILTER_KEY, bytes);
                }
            }

            Set<byte[]> deleteBytes = null;
            deletes = this.tableNameToDeletes.remove(tableName);
            if (deletes != null) {
                deleteBytes = new HashSet<>(deletes.size());
                for (String delete : deletes) {
                    byte[] key = delete.getBytes(StandardCharsets.UTF_8);
                    totalBytes += key.length;
                    deleteBytes.add(key);
                }
            }
            long elapsedTime = System.currentTimeMillis() - now;
            commit(tableName, putBytes, deleteBytes);
            timestamp = System.currentTimeMillis();
            tableNameToTimestamps.put(tableName, timestamp);
            logger.info("commit data to db [{}][{}][{}][{}][{}][{}][{}]", path(), tableName, putBytes == null ? 0 : putBytes.size(), deleteBytes == null ? 0 : deleteBytes.size(), byteToString(totalBytes), elapsedTime, timestamp - now);
        });
    }

    protected abstract List<byte[]> get(String tableName, List<byte[]> keys);

    protected abstract byte[] get(String tableName, byte[] key);

    protected abstract void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes);

    @Override
    public boolean mightContain(String tableName, String key) {
        if (bloomFilterEnabled) {
            return getBloomFilter(tableName).mightContain(key);
        }
        return false;
    }

    @Override
    public Lock writeLock(String tableName) {
        return this.locks.computeIfAbsent(tableName, s -> new ReentrantReadWriteLock()).writeLock();
    }

    @Override
    public Lock readLock(String tableName) {
        return this.locks.computeIfAbsent(tableName, s -> new ReentrantReadWriteLock()).readLock();
    }

    @Override
    public boolean dropTable(String tableName) {
        this.tableNameToPuts.remove(tableName);
        this.tableNameToDeletes.remove(tableName);
        this.tableNameToTimestamps.remove(tableName);
        this.tableNameToResidentMemories.remove(tableName);
        this.tableNameToBloomFilters.remove(tableName);
        return true;
    }

    @Override
    public Set<String> getTableNames() {
        Set<String> tableNames = new HashSet<>();
        tableNames.addAll(this.tableNameToPuts.keySet());
        tableNames.addAll(this.tableNameToDeletes.keySet());
        tableNames.addAll(this.tableNameToTimestamps.keySet());
        tableNames.addAll(this.tableNameToResidentMemories.keySet());
        tableNames.addAll(this.tableNameToBloomFilters.keySet());
        return tableNames;
    }

    protected <T> T readLock(String tableName, Function0<T> function) {
        try {
            this.readLock(tableName).lock();
            return function.apply();
        } finally {
            this.readLock(tableName).unlock();
        }
    }

    protected void writeLock(String tableName, Consumer0 consumer) {
        try {
            this.writeLock(tableName).lock();
            consumer.accept();
        } finally {
            this.writeLock(tableName).unlock();
        }
    }

    private String byteToString(long byteLength) {
        if (byteLength < 1024) {
            return byteLength + "B";
        }
        byteLength = byteLength / 1024;
        if (byteLength < 1024) {
            return byteLength + "KB";
        }
        byteLength = byteLength / 1024;
        if (byteLength < 1024) {
            return byteLength + "MB";
        }
        byteLength = byteLength / 1024;
        return byteLength + "GB";
    }

    protected byte[] toBytes(Object value) {
        try {
            return this.serialization.toBytes(value);
        } catch (Exception cause) {
            this.logger.error("toBytes", cause);
            return null;
        }
    }

    protected Object fromBytes(byte[] bytes) {
        try {
            return this.serialization.fromBytes(bytes);
        } catch (Exception cause) {
            this.logger.error("toObject", cause);
            return null;
        }
    }

    protected BloomFilter<String> getBloomFilter(String tableName) {
        return this.tableNameToBloomFilters.computeIfAbsent(tableName, key -> {
            byte[] bytes = get(tableName, BLOOM_FILTER_KEY);
            if (bytes == null) {
                return BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), this.expectedInsertions, this.fpp);
            }
            try {
                return BloomFilter.readFrom(new ByteArrayInputStream(bytes), Funnels.stringFunnel(StandardCharsets.UTF_8));
            } catch (IOException cause) {
                throw new RuntimeException(cause);
            }
        });
    }

    private byte[] toBytes(BloomFilter<String> bloomFilter) {
        if (bloomFilter == null) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            bloomFilter.writeTo(out);
            return out.toByteArray();
        } catch (IOException cause) {
            this.logger.error("bloomFilter writeTo", cause);
            return null;
        }
    }

}

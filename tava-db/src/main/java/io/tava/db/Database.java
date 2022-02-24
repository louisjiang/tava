package io.tava.db;

import io.tava.function.Consumer2;
import io.tava.function.Consumer3;
import io.tava.lang.Tuple2;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-07 14:37
 */
public interface Database {

    default void put(Map<String, Object> keyValues) {
        this.put("default", keyValues);
    }

    void put(String tableName, Map<String, Object> keyValues);

    default void put(String key, Object value) {
        this.put("default", key, value);
    }

    void put(String tableName, String key, Object value);

    default void delete(Set<String> keys) {
        this.delete("default", keys);
    }

    void delete(String tableName, Set<String> keys);

    default void delete(String key) {
        this.delete("default", key);
    }

    void delete(String tableName, String key);

    default Object get(String key) {
        return this.get("default", key);
    }

    Object get(String tableName, String key);

    default Map<String, Object> get(Set<String> keys) {
        return this.get("default", keys);
    }

    Map<String, Object> get(String tableName, Set<String> keys);

    default Iterator iterator() {
        return this.iterator("default");
    }

    default Iterator iterator(String tableName) {
        return this.iterator(tableName, false);
    }

    Iterator iterator(String tableName, boolean useSnapshot);

    void tryCommit(String tableName);

    void commit(String tableName);

    Tuple2<Boolean, byte[]> keyMayExist(String tableName, String key);

    String path();

    Lock writeLock(String tableName);

    Lock readLock(String tableName);

    boolean createTable(String tableName);

    boolean dropTable(String tableName);

    Set<String> getTableNames();

    void close();

    byte[] toBytes(Object value);

    Object toObject(byte[] bytes);

    void compact(String tableName);

    void compact();

    void addCommitCallback(String tableName, Consumer3<String, byte[], byte[]> putCallback, Consumer2<String, byte[]> deleteCallback);

}

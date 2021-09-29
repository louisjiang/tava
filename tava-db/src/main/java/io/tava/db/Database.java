package io.tava.db;

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

    default Object get(String key, boolean forUpdate) {
        return this.get("default", key, forUpdate);
    }

    Object get(String tableName, String key, boolean forUpdate);

    default Map<String, Object> get(Set<String> keys) {
        return this.get("default", keys);
    }

    Map<String, Object> get(String tableName, Set<String> keys);

    default Iterator iterator() {
        return this.iterator("default");
    }

    Iterator iterator(String tableName);

    void tryCommit(String tableName);

    void commit(String tableName, boolean force);

    void addResidentMemory(String tableName, String key);

    void removeResidentMemory(String tableName, String key);

    String path();

    Lock writeLock(String tableName);

    Lock readLock(String tableName);

    boolean createTable(String tableName);

    boolean dropTable(String tableName);

    Set<String> getTableNames();

    void close();

}

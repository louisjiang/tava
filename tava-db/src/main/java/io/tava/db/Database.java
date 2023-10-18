package io.tava.db;

import io.tava.db.segment.Segment;
import io.tava.db.segment.SegmentList;
import io.tava.db.segment.SegmentMap;
import io.tava.db.segment.SegmentSet;
import io.tava.function.*;
import io.tava.lang.Option;

import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-07 14:37
 */
public interface Database {

    default <V> SegmentList<V> newSegmentList(String key, int capacity) {
        return newSegmentList("default", key, capacity);
    }

    <V> SegmentList<V> newSegmentList(String tableName, String key, int capacity);

    default <V> Option<SegmentList<V>> getSegmentList(String key) {
        return getSegmentList("default", key);
    }

    <V> Option<SegmentList<V>> getSegmentList(String tableName, String key);

    default <V> SegmentSet<V> newSegmentSet(String key, int segment) {
        return newSegmentSet("default", key, segment);
    }

    <V> SegmentSet<V> newSegmentSet(String tableName, String key, int segment);

    default <V> Option<SegmentSet<V>> getSegmentSet(String key) {
        return getSegmentSet("default", key);
    }

    <V> Option<SegmentSet<V>> getSegmentSet(String tableName, String key);

    default <K, V> SegmentMap<K, V> newSegmentMap(String key, int segment) {
        return newSegmentMap("default", key, segment);
    }

    <K, V> SegmentMap<K, V> newSegmentMap(String tableName, String key, int segment);

    default <K, V> Option<SegmentMap<K, V>> getSegmentMap(String key) {
        return getSegmentMap("default", key);
    }

    <K, V> Option<SegmentMap<K, V>> getSegmentMap(String tableName, String key);

    default void put(Map<String, Object> keyValues) {
        this.put("default", keyValues);
    }

    default void put(String tableName, Map<String, Object> keyValues) {
        for (Map.Entry<String, Object> entry : keyValues.entrySet()) {
            this.put(tableName, entry.getKey(), entry.getValue());
        }
    }

    default void put(String key, Object value) {
        this.put("default", key, value);
    }

    void put(String tableName, String key, Object value);

    default void delete(Set<String> keys) {
        this.delete("default", keys);
    }

    default void delete(String tableName, Set<String> keys) {
        for (String key : keys) {
            this.delete(tableName, key);
        }
    }

    default void delete(String key) {
        this.delete("default", key);
    }

    void delete(String tableName, String key);

    default <T> T get(String key) {
        return this.get("default", key);
    }

    <T> T get(String tableName, String key);

    default <T> T update(String key, Function1<T, T> update) {
        return this.update("default", key, update);
    }

    <T> T update(String tableName, String key, Function1<T, T> update);

    default <T> void get(String key, Consumer1<T> consumer1) {
        get("default", key, consumer1);
    }

    <T> void get(String tableName, String key, Consumer1<T> consumer1);

    default Iterator iterator() {
        return this.iterator("default");
    }

    default Iterator iterator(String tableName) {
        return this.iterator(tableName, false);
    }

    Iterator iterator(String tableName, boolean useSnapshot);

    void tryCommit(String tableName);

    void commit(String tableName);

    boolean keyMayExist(String tableName, String key);

    String path();

    void writeLock(String key);

    void unWriteLock(String key);

    void writeLock(String key, Consumer0 consumer);

    <T> T writeLock(String key, Function0<T> function);

    void readLock(String key);

    void unReadLock(String key);

    void readLock(String key, Consumer0 consumer);

    <T> T readLock(String key, Function0<T> function);

    boolean createTable(String tableName);

    boolean dropTable(String tableName);

    Set<String> getTableNames();

    void close();

    byte[] toBytes(Object value);

    Object toObject(byte[] bytes);

    void compact(String tableName);

    void compact();

    void addCommitCallback(String tableName, Consumer3<String, byte[], byte[]> putCallback, Consumer2<String, byte[]> deleteCallback);

    void updateSegmentCache(String key, Segment segment);
}

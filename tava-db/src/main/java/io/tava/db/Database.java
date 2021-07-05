package io.tava.db;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-07 14:37
 */
public interface Database<K, V> {

    void put(K key, V value);

    void delete(K key);

    V get(K key);

    WriteBatch<K, V> writeBatch();

    String path();

    DatabaseType type();

    void close();

}

package io.tava.db;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:42
 */
public interface WriteBatch<K, V> {

    void put(K key, V value);

    void delete(K key);

    int size();

    void commit();

}

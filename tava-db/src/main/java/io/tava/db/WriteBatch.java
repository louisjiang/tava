package io.tava.db;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:42
 */
public interface WriteBatch {

    void delete(byte[] key);

    void put(byte[] key, byte[] value);

    void commit();

}

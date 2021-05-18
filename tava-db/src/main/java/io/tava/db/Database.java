package io.tava.db;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-07 14:37
 */
public interface Database {

    void put(byte[] key, byte[] value);

    void delete(byte[] key);

    byte[] get(byte[] key);

    WriteBatch writeBatch();

    void close();

}

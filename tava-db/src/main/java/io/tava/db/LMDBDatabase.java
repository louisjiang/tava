package io.tava.db;

import io.tava.configuration.Configuration;
import io.tava.lang.Tuple2;
import io.tava.serialization.Serialization;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-07-13 11:40
 */
public class LMDBDatabase extends AbstractDatabase {

    public LMDBDatabase(Configuration configuration, Serialization serialization) {
        super(configuration, serialization);
    }

    @Override
    protected List<byte[]> get(String tableName, List<byte[]> keys) {
        return null;
    }

    @Override
    protected byte[] get(String tableName, byte[] key) {
        return new byte[0];
    }

    @Override
    protected void commit(String tableName, Map<byte[], byte[]> puts, Set<byte[]> deletes) {

    }

    @Override
    public Iterator iterator(String tableName, boolean useSnapshot) {
        return null;
    }

    @Override
    public boolean keyMayExist(String tableName, String key) {
        return false;
    }

    @Override
    public String path() {
        return null;
    }

    @Override
    public boolean createTable(String tableName) {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public void compact(String tableName) {

    }

    @Override
    public void compact() {

    }
}

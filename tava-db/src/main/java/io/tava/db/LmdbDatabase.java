package io.tava.db;

import io.tava.Tava;
import io.tava.configuration.Configuration;
import io.tava.lang.Tuple2;
import io.tava.serialization.Serialization;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-14 12:52
 */
public class LmdbDatabase extends AbstractDatabase {

    public LmdbDatabase(Configuration configuration, Serialization serialization) {
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
    public Iterator iterator(String tableName) {
        return null;
    }

    @Override
    public Tuple2<Boolean, byte[]> mightContain(String tableName, String key) {
        return Tava.of(false, null);
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
    public boolean dropTable(String tableName) {
        return false;
    }

    @Override
    public Set<String> getTableNames() {
        return null;
    }

    @Override
    public void close() {

    }
}

package io.tava.db;

import io.tava.db.util.Serialization;

import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-07 22:06
 */
public final class ObjectDatabase implements Database<String, Object> {

    private final Database<byte[], byte[]> delegate;

    public ObjectDatabase(Database<byte[], byte[]> database) {
        this.delegate = database;
    }

    @Override
    public void put(String key, Object value) {
        this.delegate.put(Serialization.toBytes(key), Serialization.toBytes(value));
    }

    @Override
    public void delete(String key) {
        this.delegate.delete(Serialization.toBytes(key));
    }

    @Override
    public Object get(String key) {
        byte[] bytes = this.delegate.get(Serialization.toBytes(key));
        return Serialization.toObject(bytes);
    }

    @Override
    public WriteBatch<String, Object> writeBatch() {
        return new AbstractWriteBatch<String, Object>() {
            @Override
            public void commit() {
                WriteBatch<byte[], byte[]> writeBatch = delegate.writeBatch();
                Set<Map.Entry<String, Object>> entries = this.puts.entrySet();
                for (Map.Entry<String, Object> entry : entries) {
                    writeBatch.put(Serialization.toBytes(entry.getKey()), Serialization.toBytes(entry.getValue()));
                }
                for (String delete : this.deletes) {
                    writeBatch.delete(Serialization.toBytes(delete));
                }
                writeBatch.commit();
                this.puts.clear();
                this.deletes.clear();
            }

        };
    }

    @Override
    public String path() {
        return this.delegate.path();
    }

    @Override
    public void close() {
        this.delegate.close();
    }

    @Override
    public DatabaseType type() {
        return this.delegate.type();
    }
}

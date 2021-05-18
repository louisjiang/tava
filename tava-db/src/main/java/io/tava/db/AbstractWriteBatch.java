package io.tava.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:46
 */
public abstract class AbstractWriteBatch implements WriteBatch {

    protected Map<byte[], byte[]> puts = new HashMap<>();
    protected Set<byte[]> deletes = new HashSet<>();

    @Override
    public void delete(byte[] key) {
        this.puts.remove(key);
        this.deletes.add(key);
    }

    @Override
    public void put(byte[] key, byte[] value) {
        this.puts.put(key, value);
        this.deletes.remove(key);
    }
}

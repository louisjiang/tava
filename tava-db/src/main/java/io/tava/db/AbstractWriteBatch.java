package io.tava.db;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 13:46
 */
public abstract class AbstractWriteBatch<K, V> implements WriteBatch<K, V> {

    protected final Map<K, V> puts = new HashMap<>();
    protected final Set<K> deletes = new HashSet<>();

    @Override
    public void put(K key, V value) {
        this.puts.put(key, value);
        this.deletes.remove(key);
    }

    @Override
    public void delete(K key) {
        this.puts.remove(key);
        this.deletes.add(key);
    }

    @Override
    public int size() {
        return this.puts.size() + this.deletes.size();
    }

}

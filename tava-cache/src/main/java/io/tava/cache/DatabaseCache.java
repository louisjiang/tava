package io.tava.cache;

import io.tava.db.Database;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 14:09
 */
public class DatabaseCache<V> extends MemoryCache<V> {

    private final Database database;

    public DatabaseCache(com.github.benmanes.caffeine.cache.LoadingCache<String, V> cache,
                         Database database) {
        super(cache);
        this.database = database;
    }

    @Override
    public void put(String key, V value) {
        super.put(key, value);
        this.database.put(key, value);
    }

    @Override
    public void delete(String key) {
        super.delete(key);
        this.database.delete(key);
    }

}

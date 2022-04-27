package io.tava.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import io.tava.db.Database;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 13:58
 */
public class DatabaseCacheLoader<V> implements CacheLoader<String, V> {

    private final Database database;

    public DatabaseCacheLoader(Database database) {
        this.database = database;
    }

    @Override
    @Nullable
    public V load(@NonNull String key) throws Exception {
        return this.database.get(key);
    }

}

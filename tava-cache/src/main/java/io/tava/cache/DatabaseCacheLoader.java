package io.tava.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import io.tava.db.ObjectDatabase;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 13:58
 */
public class DatabaseCacheLoader implements CacheLoader<String, Object> {

    private final ObjectDatabase database;

    public DatabaseCacheLoader(ObjectDatabase database) {
        this.database = database;
    }

    @Override
    @Nullable
    public Object load(@NonNull String key) throws Exception {
        return database.get(key);
    }

}

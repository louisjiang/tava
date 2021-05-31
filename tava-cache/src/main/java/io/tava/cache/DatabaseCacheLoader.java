package io.tava.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import io.tava.db.Database;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.nio.charset.StandardCharsets;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 13:58
 */
public class DatabaseCacheLoader implements CacheLoader<String, Object> {

    private final Database database;

    public DatabaseCacheLoader(Database database) {
        this.database = database;
    }

    @Override
    public @Nullable Object load(@NonNull String key) throws Exception {
        byte[] bytes = database.get(key.getBytes(StandardCharsets.UTF_8));
        return bytes;
    }

}

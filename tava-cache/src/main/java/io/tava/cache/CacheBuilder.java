package io.tava.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.tava.db.Database;

import java.util.concurrent.TimeUnit;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 11:43
 */
public final class CacheBuilder {

    private final Caffeine<Object, Object> caffeine;
    private Database database;

    public CacheBuilder() {
        this.caffeine = Caffeine.newBuilder();
    }

    public CacheBuilder initialCapacity(int initialCapacity) {
        this.caffeine.initialCapacity(initialCapacity);
        return this;
    }

    public CacheBuilder maximumSize(long maximumSize) {
        this.caffeine.maximumSize(maximumSize);
        return this;
    }

    public CacheBuilder weakValues() {
        this.caffeine.weakValues();
        return this;
    }

    public CacheBuilder weakKeys() {
        this.caffeine.weakKeys();
        return this;
    }

    public CacheBuilder softValues() {
        this.caffeine.softValues();
        return this;
    }

    public CacheBuilder refreshAfterWrite(long duration, TimeUnit unit) {
        this.caffeine.refreshAfterWrite(duration, unit);
        return this;
    }

    public CacheBuilder expireAfterWrite(long duration, TimeUnit unit) {
        this.caffeine.expireAfterWrite(duration, unit);
        return this;
    }

    public CacheBuilder expireAfterAccess(long duration, TimeUnit unit) {
        this.caffeine.expireAfterAccess(duration, unit);
        return this;
    }

    public CacheBuilder database(Database database) {
        this.database = database;
        return this;
    }

    public Cache build() {
        if (this.database == null) {
            return new MemoryCache(this.caffeine.build());
        }
        return new DatabaseCache(this.caffeine.build(new DatabaseCacheLoader(this.database)), database);
    }

}

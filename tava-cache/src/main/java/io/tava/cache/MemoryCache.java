package io.tava.cache;


import java.util.function.Function;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-19 17:01
 */
public class MemoryCache<V> implements Cache<V> {

    private final com.github.benmanes.caffeine.cache.Cache<String, V> cache;

    public MemoryCache(com.github.benmanes.caffeine.cache.Cache<String, V> cache) {
        this.cache = cache;
    }

    @Override
    public void put(String key, V value) {
        this.cache.put(key, value);
    }

    @Override
    public final V get(String key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public final V get(String key, Function<String, V> mappingFunction) {
        return this.cache.get(key, mappingFunction);
    }

    @Override
    public void delete(String key) {
        this.cache.invalidate(key);
    }

}

package io.tava.cache;


/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-19 17:01
 */
public class MemoryCache implements Cache {

    private final com.github.benmanes.caffeine.cache.Cache<String, Object> cache;

    public MemoryCache(com.github.benmanes.caffeine.cache.Cache<String, Object> cache) {
        this.cache = cache;
    }

    @Override
    public void put(String key, Object value) {
        this.cache.put(key, value);
    }

    @Override
    public Object get(String key) {
        return this.cache.getIfPresent(key);
    }

    @Override
    public void delete(String key) {
        this.cache.invalidate(key);
    }

}

package io.tava.cache;


import com.github.benmanes.caffeine.cache.Caffeine;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-19 17:01
 */
public class CaffeineCache implements Cache {


    public CaffeineCache(){

        Caffeine<Object, Object> objectObjectCaffeine = Caffeine.newBuilder();

    }

    @Override
    public void put(String key, Object value) {

    }

    @Override
    public Object get(String key) {
        return null;
    }

    @Override
    public void delete(String key) {

    }

}

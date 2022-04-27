package io.tava.cache;

import java.util.function.Function;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-18 16:58
 */
public interface Cache<V> {

    void put(String key, V value);

    V get(String key);

    V get(String key, Function<String, V> mappingFunction);

    void delete(String key);

}

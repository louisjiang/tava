package io.tava.cache;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-18 16:58
 */
public interface Cache {

    void put(String key, Object value);

    Object get(String key);

    void delete(String key);

}

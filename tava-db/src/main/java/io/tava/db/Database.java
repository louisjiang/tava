package io.tava.db;

import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-07 14:37
 */
public interface Database {

    void put(Map<String, Object> keyValues);

    void put(String key, Object value);

    void delete(Set<String> keys);

    void delete(String key);

    Object get(String key);

    Iterator iterator();

    void commit(boolean force);

    String path();

    void close();

}

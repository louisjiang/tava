package io.tava.db;

import io.tava.lang.Tuple2;

import java.io.Closeable;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-06 14:19
 */
public interface Iterator extends Closeable {

    boolean hasNext();

    Tuple2<String, Object> next();

}

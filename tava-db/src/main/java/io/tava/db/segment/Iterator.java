package io.tava.db.segment;

import java.io.Closeable;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-04-11 10:37
 */
public interface Iterator<V> extends Closeable {

    boolean hasNext();

    V next();

}

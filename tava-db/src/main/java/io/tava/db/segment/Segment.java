package io.tava.db.segment;

import io.tava.util.Util;

public interface Segment extends Util {

    int segment();

    void commit();

    default void clear() {
        clear(true);
    }

    void clear(boolean commit);

    default void destroy() {
        destroy(true);
    }

    void destroy(boolean commit);

    void updateStatusData(Object statusData);

    <V> V getStatusData();

}

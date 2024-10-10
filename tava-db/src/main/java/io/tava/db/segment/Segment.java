package io.tava.db.segment;

import io.tava.util.Util;

public interface Segment extends Util {

    int segment();

    void commit();

    void destroy();

    void updateStatusData(Object statusData);

    <V> V getStatusData();

}

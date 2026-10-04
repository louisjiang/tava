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

    default int newSegment(int segment, int basicSegment) {
        if (segment < basicSegment) {
            return basicSegment;
        }

        int newSegment = basicSegment * 2;
        if (newSegment < segment) {
            return newSegment(segment, newSegment);
        }
        return newSegment;
    }

}

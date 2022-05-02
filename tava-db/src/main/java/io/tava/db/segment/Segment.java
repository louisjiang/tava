package io.tava.db.segment;

import io.tava.util.Util;

public interface Segment extends Util {

    void commit();

    void destroy();

}

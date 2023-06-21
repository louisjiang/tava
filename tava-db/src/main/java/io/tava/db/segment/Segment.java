package io.tava.db.segment;

import io.tava.util.Util;

import java.util.Map;

public interface Segment extends Util {

    void commit();

    void destroy();

    void setStatus(Map<String, Object> status);

    Map<String, Object> getStatus();

}

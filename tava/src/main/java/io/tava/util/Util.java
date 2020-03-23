package io.tava.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-13 11:53:11
 */
public interface Util {

    default String toString(Object... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object value : values) {
            if (value != null) {
                stringBuilder.append(value);
            }
        }
        return stringBuilder.toString();
    }


    default boolean nonNull(Object value) {
        return !isNull(value);
    }

    default boolean nonEmpty(Object value) {
        return !isEmpty(value);
    }

    default boolean isNull(Object value) {
        return value == null;
    }

    default boolean isEmpty(Object value) {
        if (isNull(value)) {
            return true;
        }

        if (value instanceof String) {
            return ((String) value).isEmpty();
        }

        if (value instanceof Collection) {
            return ((Collection<?>) value).isEmpty();
        }

        if (value instanceof Map) {
            return ((Map<?, ?>) value).isEmpty();
        }

        if (value instanceof Object[]) {
            return ((Object[]) value).length == 0;
        }

        if (value instanceof byte[]) {
            return ((byte[]) value).length == 0;
        }

        if (value instanceof short[]) {
            return ((short[]) value).length == 0;
        }

        if (value instanceof int[]) {
            return ((int[]) value).length == 0;
        }
        if (value instanceof long[]) {
            return ((long[]) value).length == 0;
        }
        if (value instanceof float[]) {
            return ((float[]) value).length == 0;
        }
        if (value instanceof double[]) {
            return ((double[]) value).length == 0;
        }

        if (value instanceof boolean[]) {
            return ((boolean[]) value).length == 0;
        }

        return value == null;
    }


}

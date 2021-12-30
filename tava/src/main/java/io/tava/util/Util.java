package io.tava.util;

import io.tava.function.Function1;

import java.util.Collection;
import java.util.Map;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-13 11:53:11
 */
public interface Util {

    default <T> String mkString(java.util.Collection<T> values, String separator) {
        return mkString(values, null, separator, null);
    }

    default <T> String mkString(java.util.Collection<T> values, String start, String separator, String end) {
        StringBuilder builder = new StringBuilder();
        if (nonEmpty(start)) {
            builder.append(start);
        }
        boolean appendSeparator = false;
        for (Object value : values) {
            appendSeparator = append_(separator, builder, appendSeparator, value);
        }
        if (nonEmpty(end)) {
            builder.append(end);
        }
        return builder.toString();
    }

    default <T> String mkString(T[] values, String separator) {
        return mkString(values, null, separator, null);
    }

    default <T> String mkString(T[] values, String start, String separator, String end) {
        StringBuilder builder = new StringBuilder();
        if (nonEmpty(start)) {
            builder.append(start);
        }
        boolean appendSeparator = false;
        for (Object value : values) {
            appendSeparator = append_(separator, builder, appendSeparator, value);
        }
        if (nonEmpty(end)) {
            builder.append(end);
        }
        return builder.toString();
    }

    default boolean append_(String separator, StringBuilder builder, boolean appendSeparator, Object value) {
        if (isEmpty(value)) {
            return appendSeparator;
        }
        if (appendSeparator) {
            builder.append(separator);
        }
        builder.append(value);
        return true;
    }


    default <T> String toString(T... values) {
        if (values == null || values.length == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (Object value : values) {
            if (isEmpty(value)) {
                continue;
            }
            builder.append(value);
        }
        return builder.toString();
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


    default String toCase(String value, int startIndex, int endIndex, Function1<Character, Character> transform) {
        if (isEmpty(value)) {
            return value;
        }
        char[] chars = value.toCharArray();
        int length = chars.length;
        if (startIndex > length) {
            throw new StringIndexOutOfBoundsException(startIndex + " > " + length);
        }
        if (endIndex > length) {
            throw new StringIndexOutOfBoundsException(endIndex + " > " + length);
        }
        int index = startIndex;
        while (index < endIndex) {
            chars[index] = transform.apply(chars[index]);
            index++;
        }
        return new String(chars);
    }

    default String toLowerCase(String value, int startIndex, int endIndex) {
        return toCase(value, startIndex, endIndex, character -> {
            if (character >= 'A' && character <= 'Z') {
                return (char) (character + 32);
            }
            return character;
        });
    }

    default String toUpperCase(String value, int startIndex, int endIndex) {
        return toCase(value, startIndex, endIndex, character -> {
            if (character >= 'a' && character <= 'z') {
                return (char) (character - 32);
            }
            return character;
        });
    }

    default String firstUpperCase(String value) {
        return toUpperCase(value, 0, 1);
    }

    default String firstLowerCase(String value) {
        return toLowerCase(value, 0, 1);
    }

}

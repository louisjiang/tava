package io.tava.reflect.util;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 17:03
 */
public class ReflectionException extends Exception {

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}

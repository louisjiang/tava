package io.tava.reflect.util;

import java.lang.reflect.Executable;
import java.lang.reflect.Field;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 13:44
 */
public class ReflectionUtil {


    public static int index(Executable executable) {
        Class<?>[] parameterTypes = executable.getParameterTypes();
        int index = executable.getName().hashCode() ^ parameterTypes.length;
        for (Class<?> parameterType : parameterTypes) {
            index = index ^ parameterType.getTypeName().hashCode();
        }
        return index;
    }

    public static int index(Field field) {
        return field.getType().getName().hashCode() ^ field.getName().hashCode();
    }

}

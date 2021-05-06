package io.tava.reflect.accessor;

import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:45
 */
public interface Accessor {

    default ConstructorAccessor get(Constructor<?> targetConstructor) throws ReflectionException {
        return get(targetConstructor, targetConstructor.getClass().getClassLoader());
    }

    ConstructorAccessor get(Constructor<?> targetConstructor, ClassLoader classLoader) throws ReflectionException;

    default FieldAccessor get(Field targetField) throws ReflectionException {
        return get(targetField, targetField.getClass().getClassLoader());
    }

    FieldAccessor get(Field targetField, ClassLoader classLoader) throws ReflectionException;

    default MethodAccessor get(Method method) throws ReflectionException {
        return get(method, method.getClass().getClassLoader());
    }

    MethodAccessor get(Method method, ClassLoader classLoader) throws ReflectionException;

}
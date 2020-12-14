package io.tava.reflect.accessor;

import io.tava.reflect.util.ReflectionException;
import io.tava.util.Map;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 16:43
 */
public interface ClassAccessor<T> {

    Class<T> targetClass();

    Map<Integer, ConstructorAccessor> constructorAccessors();

    Map<Integer, FieldAccessor> fieldAccessors();

    Map<Integer, MethodAccessor> methodAccessors();

    Field field(int fieldIndex);

    int fieldIndex(Field field);

    Method method(int methodIndex);

    int methodIndex(Method method);

    FieldAccessor fieldAccessor(int fieldIndex) throws ReflectionException;

    MethodAccessor methodAccessor(int methodIndex) throws ReflectionException;

    T newInstance(int constructorIndex, Object[] arguments) throws ReflectionException;

    Object get(int fieldIndex, Object instance) throws ReflectionException;

    Object get(String fieldName, Object instance) throws ReflectionException;

    void set(int fieldIndex, Object instance, Object argument) throws ReflectionException;

    void set(String fieldName, Object instance, Object argument) throws ReflectionException;

    Object invoke(int methodIndex, Object instance, Object[] arguments) throws ReflectionException;

}

package io.tava.reflect.accessor;

import io.tava.reflect.util.ReflectionException;
import io.tava.util.Map;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 17:35
 */
public class ClassAccessorImpl<T> implements ClassAccessor<T> {

    private final Class<T> targetClass;
    private final Map<Integer, FieldAccessor> fieldAccessors;
    private final Map<Integer, MethodAccessor> methodAccessors;
    private final Map<Integer, ConstructorAccessor> constructorAccessors;

    public ClassAccessorImpl(Class<T> targetClass,
                             Map<Integer, ConstructorAccessor> constructorAccessors,
                             Map<Integer, FieldAccessor> fieldAccessors,
                             Map<Integer, MethodAccessor> methodAccessors) {
        this.targetClass = targetClass;
        this.constructorAccessors = constructorAccessors;
        this.fieldAccessors = fieldAccessors;
        this.methodAccessors = methodAccessors;
    }

    @Override
    public Class<T> targetClass() {
        return this.targetClass;
    }

    @Override
    public Map<Integer, ConstructorAccessor> constructorAccessors() {
        return this.constructorAccessors;
    }

    @Override
    public Map<Integer, FieldAccessor> fieldAccessors() {
        return this.fieldAccessors;
    }

    @Override
    public Map<Integer, MethodAccessor> methodAccessors() {
        return this.methodAccessors;
    }

    @Override
    public Field field(int fieldIndex) {
        return null;
    }

    @Override
    public int fieldIndex(Field field) {
        return 0;
    }

    @Override
    public Method method(int methodIndex) {
        return null;
    }

    @Override
    public int methodIndex(Method method) {
        return 0;
    }

    @Override
    public FieldAccessor fieldAccessor(int fieldIndex) throws ReflectionException {
        FieldAccessor fieldAccessor = this.fieldAccessors.get(fieldIndex);
        if (fieldAccessor == null) {
            throw new ReflectionException("Not found field accessor by index:" + fieldIndex);
        }
        return fieldAccessor;
    }

    @Override
    public MethodAccessor methodAccessor(int methodIndex) throws ReflectionException {
        MethodAccessor methodAccessor = this.methodAccessors.get(methodIndex);
        if (methodAccessor == null) {
            throw new ReflectionException("Not found method accessor by index:" + methodIndex);
        }
        return methodAccessor;
    }

    @Override
    public T newInstance(int constructorIndex, Object[] arguments) throws ReflectionException {
        ConstructorAccessor constructorAccessor = this.constructorAccessors.get(constructorIndex);
        if (constructorAccessor == null) {
            throw new ReflectionException("Not found constructor accessor by index:" + constructorIndex);
        }
        return (T) constructorAccessor.newInstance(arguments);
    }

    @Override
    public Object get(int fieldIndex, Object instance) throws ReflectionException {
        return this.fieldAccessor(fieldIndex).get(instance);
    }

    @Override
    public Object get(String fieldName, Object instance) throws ReflectionException {
        return this.fieldAccessor(fieldName.hashCode()).get(instance);
    }

    @Override
    public void set(int fieldIndex, Object instance, Object argument) throws ReflectionException {
        this.fieldAccessor(fieldIndex).set(instance, argument);
    }

    @Override
    public void set(String fieldName, Object instance, Object argument) throws ReflectionException {
        this.fieldAccessor(fieldName.hashCode()).set(instance, argument);
    }

    @Override
    public Object invoke(int methodIndex, Object instance, Object[] arguments) throws ReflectionException {
        MethodAccessor methodAccessor = this.methodAccessor(methodIndex);
        return methodAccessor.invoke(instance, arguments);
    }

}

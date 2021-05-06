package io.tava.reflect.accessor;

import io.tava.lang.Option;
import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.Field;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 16:51
 */
public interface FieldAccessor {

    Field targetField();

    Object get(Object instance) throws ReflectionException;

    void set(Object instance, Object argument) throws ReflectionException;

    class StaticFieldAccessor implements FieldAccessor {

        private final Field targetField;

        public StaticFieldAccessor(Field targetField) {
            this.targetField = targetField;
        }

        @Override
        public Field targetField() {
            return targetField;
        }

        @Override
        public Object get(Object instance) throws ReflectionException {
            try {
                return targetField.get(null);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("get:" + targetField, e);
            }
        }

        @Override
        public void set(Object instance, Object argument) throws ReflectionException {
            try {
                targetField.set(null, argument);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("set:" + targetField, e);
            }
        }
    }

    class PrivateFieldAccessor implements FieldAccessor {

        private final Field targetField;
        private final Option<MethodAccessor> getMethodAccessor;
        private final Option<MethodAccessor> setMethodAccessor;

        public PrivateFieldAccessor(Field targetField,
                                    Option<MethodAccessor> getMethodAccessor,
                                    Option<MethodAccessor> setMethodAccessor) {
            this.targetField = targetField;
            this.getMethodAccessor = getMethodAccessor;
            this.setMethodAccessor = setMethodAccessor;
            this.targetField.setAccessible(true);
        }

        @Override
        public Field targetField() {
            return this.targetField;
        }

        @Override
        public Object get(Object instance) throws ReflectionException {
            if (this.getMethodAccessor.hasValue()) {
                return this.getMethodAccessor.get().invoke(instance, new Object[0]);
            }
            try {
                return this.targetField.get(instance);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("get:" + targetField, e);
            }
        }

        @Override
        public void set(Object instance, Object argument) throws ReflectionException {
            if (this.setMethodAccessor.hasValue()) {
                this.setMethodAccessor.get().invoke(instance, new Object[]{argument});
                return;
            }
            try {
                this.targetField.set(instance, argument);
            } catch (IllegalAccessException e) {
                throw new ReflectionException("set:" + targetField, e);
            }
        }
    }

}

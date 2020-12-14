package io.tava.reflect.accessor;

import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 17:13
 */
public interface ConstructorAccessor {

    Constructor<?> targetConstructor();

    <T> T newInstance(Object[] arguments) throws ReflectionException;

    class PrivateConstructorAccessor implements ConstructorAccessor {

        private final Constructor<?> targetConstructor;

        public PrivateConstructorAccessor(Constructor<?> targetConstructor) {
            this.targetConstructor = targetConstructor;
        }

        @Override
        public Constructor<?> targetConstructor() {
            return this.targetConstructor;
        }

        @Override
        public <T> T newInstance(Object[] arguments) throws ReflectionException {
            try {
                return (T) this.targetConstructor.newInstance(arguments);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException("newInstance:" + targetConstructor, e);
            }
        }
    }

}

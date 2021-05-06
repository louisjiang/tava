package io.tava.reflect.accessor;

import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 16:46
 */
public interface MethodAccessor {

    Method targetMethod();

    Object invoke(Object instance, Object[] arguments) throws ReflectionException;

    class PrivateMethodAccessor implements MethodAccessor {

        private final Method targetMethod;

        public PrivateMethodAccessor(Method targetMethod) {
            this.targetMethod = targetMethod;
        }

        @Override
        public Method targetMethod() {
            return this.targetMethod;
        }

        @Override
        public Object invoke(Object instance, Object[] arguments) throws ReflectionException {
            try {
                return this.targetMethod.invoke(instance, arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException("invoke:" + targetMethod, e);
            }
        }
    }

    class StaticMethodAccessor implements MethodAccessor {

        private final Method targetMethod;

        public StaticMethodAccessor(Method targetMethod) {
            this.targetMethod = targetMethod;
        }

        @Override
        public Method targetMethod() {
            return this.targetMethod;
        }

        @Override
        public Object invoke(Object instance, Object[] arguments) throws ReflectionException {
            try {
                return this.targetMethod.invoke(null, arguments);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ReflectionException("invoke:" + targetMethod, e);
            }
        }
    }

}

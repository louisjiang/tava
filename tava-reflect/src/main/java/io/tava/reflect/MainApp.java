package io.tava.reflect;

import io.tava.reflect.accessor.Accessor;
import io.tava.reflect.accessor.ConstructorAccessor;
import io.tava.reflect.javassist.JavassistAccessor;
import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.Constructor;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:09
 */
public class MainApp {

    public static void main(String[] args) throws ReflectionException {
        Constructor<Test> constructor =(Constructor<Test>) Test.class.getConstructors()[0];
        Accessor accessor = new JavassistAccessor();
        ConstructorAccessor constructorAccessor = accessor.get(constructor);
        Test o = constructorAccessor.newInstance(new Object[]{"bb"});
        System.out.println(constructorAccessor);

    }

}

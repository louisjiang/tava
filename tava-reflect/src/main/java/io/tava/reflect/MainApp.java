package io.tava.reflect;

import io.tava.reflect.accessor.Accessor;
import io.tava.reflect.accessor.ConstructorAccessor;
import io.tava.reflect.accessor.FieldAccessor;
import io.tava.reflect.accessor.MethodAccessor;
import io.tava.reflect.javassist.JavassistAccessor;
import io.tava.reflect.util.ReflectionException;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:09
 */
public class MainApp {

    public static void main(String[] args) throws ReflectionException, NoSuchFieldException, NoSuchMethodException {
        Class<Test> testClass = Test.class;
        Constructor<Test> constructor = (Constructor<Test>) testClass.getConstructors()[0];
        Accessor accessor = new JavassistAccessor();
        FieldAccessor fieldAccessor = accessor.get(testClass.getDeclaredField("a"));
        FieldAccessor fieldAccessorb = accessor.get(testClass.getDeclaredField("b"));


        MethodAccessor getA = accessor.get(testClass.getDeclaredMethod("getA"));

        MethodAccessor setA = accessor.get(testClass.getDeclaredMethod("setA", String.class));


        ConstructorAccessor constructorAccessor = accessor.get(constructor);

        Test o = constructorAccessor.newInstance(new Object[]{"bb"});
        Object object = getA.invoke(o, null);
        System.out.println(object);
        setA.invoke(o, new Object[]{"aa"});
        object = getA.invoke(o, null);
        System.out.println(object);
        fieldAccessor.set(o, "cc");
        Object o1 = fieldAccessor.get(o);
        System.out.println(o1);

        fieldAccessorb.set(o, "bbb");
        Object o2 = fieldAccessorb.get(o);
        System.out.println(o2);
    }
}

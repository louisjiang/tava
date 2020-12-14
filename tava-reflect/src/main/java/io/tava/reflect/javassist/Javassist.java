package io.tava.reflect.javassist;

import javassist.ClassPool;
import javassist.LoaderClassPath;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-23 16:03
 */
public class Javassist {

    private static final Javassist javassist = new Javassist();

    private final ClassPool classPool;

    public static Javassist getInstance() {
        return javassist;
    }

    private Javassist() {
        this.classPool = ClassPool.getDefault();
        classPool.importPackage("java.lang.reflect");
        classPool.importPackage("java.util");
        classPool.importPackage("io.tava.reflect");
        classPool.importPackage("io.tava.reflect.util");
        classPool.importPackage("org.aopalliance.intercept");
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader classLoader = classPool.getClass().getClassLoader();
        if (contextClassLoader != classLoader) {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }
    }

    public ClassPool getClassPool() {
        return classPool;
    }

    public String unbox(Class<?> clazz, String value) {
        String name = clazz.getName();
        if (name.equals("void")) {
            return value;
        }

        if (name.equals("int")) {
            return value + " instanceof Integer ? ((Integer) " + value + ").intValue() : Integer.parseInt(" + value + ".toString())";
        }

        if (name.equals("long")) {
            return value + " instanceof Long ? ((Long) " + value + ").longValue() : Long.parseLong(" + value + ".toString())";
        }

        if (name.equals("double")) {
            return value + " instanceof Double ? ((Double) " + value + ").doubleValue() : Double.parseDouble(" + value + ".toString())";
        }

        if (name.equals("float")) {
            return value + " instanceof Float ? ((Float) " + value + ").floatValue() : Float.parseFloat(" + value + ".toString())";
        }

        if (name.equals("boolean")) {
            return value + " instanceof Boolean ? ((Boolean) " + value + ").booleanValue() : Boolean.parseBoolean(" + value + ".toString())";
        }

        if (name.equals("char")) {
            return value + " instanceof Character ? ((Character) " + value + ").charValue() : " + value + ".toString().toCharArray()[0]";
        }

        if (name.equals("short")) {
            return value + " instanceof Short ? ((Short) " + value + ").shortValue() : Short.parseShort(" + value + ".toString())";
        }

        if (name.equals("byte")) {
            return value + " instanceof Byte ? ((Byte) " + value + ").byteValue() : Byte.parseByte(" + value + ".toString())";
        }

        if (clazz.isArray()) {
            name = clazz.getComponentType().getName() + "[]";
        }
        return "(" + name + ")" + value;
    }


    public String box(Class<?> clazz, String value) {
        String name = clazz.getName();
        if (name.equals("int")) {
            return "Integer.valueOf(" + value + ")";
        }

        if (name.equals("long")) {
            return "Long.valueOf(" + value + ")";
        }

        if (name.equals("double")) {
            return "Double.valueOf(" + value + ")";
        }

        if (name.equals("float")) {
            return "Float.valueOf(" + value + ")";
        }

        if (name.equals("boolean")) {
            return "Boolean.valueOf(" + value + ")";
        }

        if (name.equals("char")) {
            return "Character.valueOf(" + value + ")";
        }

        if (name.equals("short")) {
            return "Short.valueOf(" + value + ")";
        }

        if (name.equals("byte")) {
            return "Byte.valueOf(" + value + ")";
        }

        if (clazz.isArray()) {
            name = clazz.getComponentType().getName() + "[]";
        }

        return "((" + name + ") " + value + ")";
    }

}

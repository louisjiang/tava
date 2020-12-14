package io.tava.reflect.javassist;

import io.tava.reflect.accessor.Accessor;
import io.tava.reflect.accessor.ConstructorAccessor;
import io.tava.reflect.accessor.FieldAccessor;
import io.tava.reflect.util.ReflectionUtil;
import io.tava.util.Map;
import io.tava.util.concurrent.ConcurrentHashMap;
import javassist.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringJoiner;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:45
 */
public class JavassistAccessor implements Accessor {

    private final Map<Constructor<?>, ConstructorAccessor> constructorAccessors = new ConcurrentHashMap<>();
    private final Map<Field, FieldAccessor> fieldAccessors = new ConcurrentHashMap<>();
    private final Map<Method, FieldAccessor> methodAccessors = new ConcurrentHashMap<>();

    private final ClassPool classPool;

    public JavassistAccessor() {
        this(null);
    }

    public JavassistAccessor(ClassLoader classLoader) {
        this.classPool = ClassPool.getDefault();
        this.classPool.importPackage("java.lang.reflect");
        this.classPool.importPackage("java.util");
        this.classPool.importPackage("io.tava.reflect");
        this.classPool.importPackage("io.tava.reflect.util");
        this.classPool.importPackage("org.aopalliance.intercept");
        if (classLoader != null) {
            classPool.appendClassPath(new LoaderClassPath(classLoader));
        }
    }

    @Override
    public ConstructorAccessor get(Constructor<?> targetConstructor, ClassLoader classLoader) {
        return constructorAccessors.computeIfAbsent(targetConstructor, constructor -> {
            int modifiers = constructor.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                return new ConstructorAccessor.PrivateConstructorAccessor(constructor);
            }
            String targetClassName = targetConstructor.getDeclaringClass().getName();
            String accessorClassName = targetClassName + "$ConstructorAccessor$" + Math.abs(ReflectionUtil.index(constructor));
            CtClass ctClass;
            try {
                ctClass = this.classPool.get(accessorClassName);
            } catch (NotFoundException e) {
                ctClass = this.classPool.makeClass(accessorClassName);
                ctClass.defrost();
                try {
                    ctClass.addInterface(this.classPool.get(ConstructorAccessor.class.getName()));
                    ctClass.addField(CtField.make("private final Constructor constructor;", ctClass));
                    CtClass[] parameters = {this.classPool.get(Constructor.class.getName())};
                    CtConstructor ctConstructor = new CtConstructor(parameters, ctClass);
                    ctConstructor.setBody("{\n\tthis.constructor = $1;\n}");
                    ctClass.addConstructor(ctConstructor);
                    String targetConstructor1 = "public Constructor targetConstructor() { \n\treturn this.constructor;\n}";
                    ctClass.addMethod(CtMethod.make(targetConstructor1, ctClass));
                    StringJoiner arguments = new StringJoiner(", ");
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    for (int i = 0; i < parameterTypes.length; i++) {
                        arguments.add(Javassist.getInstance().unbox(parameterTypes[i], "arguments[" + i + "]"));
                    }
                    String newInstance = "public Object newInstance(Object[] arguments) throws ReflectionException {\n\treturn new " + targetClassName + "(" + arguments + ");\n}";
                    ctClass.addMethod(CtMethod.make(newInstance, ctClass));
                } catch (NotFoundException | CannotCompileException cause) {
                    cause.printStackTrace();
                }
            }
            try {
                Class<?> clazz = ctClass.toClass(classLoader, null);
                return (ConstructorAccessor) clazz.getConstructor(Constructor.class).newInstance(constructor);
            } catch (CannotCompileException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        });
    }


    @Override
    public FieldAccessor get(Field targetField, ClassLoader classLoader) {
        return null;
    }
}

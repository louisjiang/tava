package io.tava.reflect.javassist;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import io.tava.lang.Option;
import io.tava.reflect.accessor.Accessor;
import io.tava.reflect.accessor.ConstructorAccessor;
import io.tava.reflect.accessor.FieldAccessor;
import io.tava.reflect.accessor.MethodAccessor;
import io.tava.reflect.util.ReflectionException;
import io.tava.reflect.util.ReflectionUtil;
import io.tava.util.Util;
import javassist.*;

import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:45
 */
public class JavassistAccessor implements Accessor, Util {

    private final Map<Constructor<?>, ConstructorAccessor> constructorAccessors = new ConcurrentHashMap<>();
    private final Map<Field, FieldAccessor> fieldAccessors = new ConcurrentHashMap<>();
    private final Map<Method, MethodAccessor> methodAccessors = new ConcurrentHashMap<>();

    private final ClassPool classPool;
    private final Mustache invokeMethodMustache;
    private final Mustache setMethodMustache;
    private final Mustache getMethodMustache;

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
        MustacheFactory mf = new DefaultMustacheFactory();
        this.invokeMethodMustache = mf.compile("invokeMethod.mustache");
        this.setMethodMustache = mf.compile("setMethod.mustache");
        this.getMethodMustache = mf.compile("getMethod.mustache");
    }

    @Override
    public ConstructorAccessor get(Constructor<?> targetConstructor, ClassLoader classLoader) throws ReflectionException {
        return constructorAccessors.computeIfAbsent(targetConstructor, constructor -> {
            int modifiers = constructor.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                return new ConstructorAccessor.PrivateConstructorAccessor(constructor);
            }
            String targetClassName = constructor.getDeclaringClass().getName();
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
    public FieldAccessor get(Field targetField, ClassLoader classLoader) throws ReflectionException {
        return fieldAccessors.computeIfAbsent(targetField, field -> {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers)) {
                return new FieldAccessor.StaticFieldAccessor(field);
            }
            String fieldName = field.getName();
            Class<?> targetClass = field.getDeclaringClass();
            if (Modifier.isPublic(modifiers)) {
                String targetClassName = targetClass.getName();
                String accessorName = toString(targetClassName, "$FieldAccessor$", fieldName, "$", ReflectionUtil.index(field)).replaceAll("-", "_");
                CtClass ctClass;
                try {
                    ctClass = classPool.get(accessorName);
                } catch (NotFoundException e) {
                    ctClass = makeClass(field, targetClassName, accessorName);
                }
                try {
                    Class<FieldAccessor> fieldAccessorClass = (Class<FieldAccessor>) ctClass.toClass(classLoader, null);
                    return fieldAccessorClass.getConstructor(Field.class).newInstance(field);
                } catch (CannotCompileException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException cause) {
                    throw new ReflectionException("get method", cause);
                }
            }

            Class<?> fieldType = field.getType();
            String prefix = "get";
            if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                prefix = "is";
            }
            String firstUpperCase = firstUpperCase(fieldName);
            MethodAccessor getMethodAccessor = null;
            try {
                Method getMethod = targetClass.getDeclaredMethod(prefix + firstUpperCase);
                getMethodAccessor = get(getMethod, classLoader);
            } catch (NoSuchMethodException ignored) {
            }

            MethodAccessor setMethodAccessor = null;
            try {
                Method setMethod = targetClass.getDeclaredMethod("set" + firstUpperCase, field.getType());
                setMethodAccessor = get(setMethod, classLoader);
            } catch (NoSuchMethodException ignored) {
            }
            return new FieldAccessor.PrivateFieldAccessor(field, Option.option(getMethodAccessor), Option.option(setMethodAccessor));
        });

    }


    @Override
    public MethodAccessor get(Method targetMethod, ClassLoader classLoader) throws ReflectionException {
        return methodAccessors.computeIfAbsent(targetMethod, method -> {
            int modifiers = method.getModifiers();
            if (Modifier.isPrivate(modifiers)) {
                return new MethodAccessor.PrivateMethodAccessor(method);
            }

            if (Modifier.isStatic(modifiers)) {
                return new MethodAccessor.StaticMethodAccessor(method);
            }

            String methodName = method.getName();
            Class<?> targetClass = method.getDeclaringClass();
            String targetClassName = targetClass.getName();
            int index = ReflectionUtil.index(method);
            String accessorName = toString(targetClassName, "$MethodAccessor$", methodName, "$", index).replaceAll("-", "_");
            CtClass ctClass;
            try {
                ctClass = classPool.get(accessorName);
            } catch (NotFoundException e) {
                ctClass = makeClass(method, targetClassName, accessorName);
            }
            try {
                Class<MethodAccessor> methodAccessorClass = (Class<MethodAccessor>) ctClass.toClass(classLoader, null);
                return methodAccessorClass.getConstructor(Method.class).newInstance(method);
            } catch (CannotCompileException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException cause) {
                throw new ReflectionException("get method", cause);
            }
        });

    }

    private CtClass makeClass(Field field, String targetClassName, String accessorName) {
        try {
            String fieldTypeName = field.getType().getName();
            CtClass ctClass = classPool.makeClass(accessorName);
            ctClass.defrost();
            ctClass.addInterface(classPool.get(FieldAccessor.class.getName()));
            ctClass.addField(CtField.make("private final Field targetField = null;", ctClass));
            CtClass[] parameters = new CtClass[]{classPool.get(Field.class.getName())};
            CtConstructor ctConstructor = new CtConstructor(parameters, ctClass);
            ctConstructor.setBody("{\n\tthis.targetField = $1;\n}");
            ctClass.addConstructor(ctConstructor);
            ctClass.addMethod(CtMethod.make("public Field targetField() {\n\treturn this.targetField;\t}", ctClass));

            Map<String, Object> scope = new HashMap<>();
            scope.put("className", targetClassName);
            scope.put("fieldName", field.getName());
            scope.put("fieldTypeName", fieldTypeName);

            String getMethod = getMethodMustache.execute(new StringWriter(), scope).toString();
            String setMethod = setMethodMustache.execute(new StringWriter(), scope).toString();

            ctClass.addMethod(CtMethod.make(getMethod, ctClass));
            ctClass.addMethod(CtMethod.make(setMethod, ctClass));
            return ctClass;

        } catch (NotFoundException | CannotCompileException cause) {
            throw new ReflectionException("makeClass", cause);
        }
    }

    private CtClass makeClass(Method method, String targetClassName, String accessorName) {
        try {
            CtClass ctClass = classPool.makeClass(accessorName);
            ctClass.defrost();
            ctClass.addInterface(classPool.get(MethodAccessor.class.getName()));
            ctClass.addField(CtField.make("private final Method targetMethod = null;", ctClass));
            CtClass[] parameters = new CtClass[]{classPool.get(Method.class.getName())};
            CtConstructor ctConstructor = new CtConstructor(parameters, ctClass);
            ctConstructor.setBody("{\n\tthis.targetMethod = $1;\n}");
            ctClass.addConstructor(ctConstructor);
            ctClass.addMethod(CtMethod.make("public Method targetMethod() {\n\treturn this.targetMethod;\t}", ctClass));
            Class<?>[] parameterTypes = method.getParameterTypes();
            List<String> arguments = new ArrayList<>();
            for (int index = 0; index < parameterTypes.length; index++) {
                arguments.add(Javassist.getInstance().unbox(parameterTypes[index], "arguments[" + index + "]"));
            }
            Class<?> returnType = method.getReturnType();
            boolean hasReturn = !"void".equals(returnType.getName());

            Map<String, Object> scope = new HashMap<>();
            scope.put("hasReturn", hasReturn);
            scope.put("className", targetClassName);
            scope.put("methodName", method.getName());
            scope.put("arguments", mkString(arguments, ", "));
            String invokeMethod = invokeMethodMustache.execute(new StringWriter(), scope).toString();
            ctClass.addMethod(CtMethod.make(invokeMethod, ctClass));
            return ctClass;
        } catch (NotFoundException | CannotCompileException cause) {
            throw new ReflectionException("makeClass", cause);
        }
    }
}

package io.tava.reflect.accessor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-11-24 15:45
 */
public interface Accessor {

    default ConstructorAccessor get(Constructor<?> targetConstructor) {
        return get(targetConstructor, targetConstructor.getClass().getClassLoader());
    }

    ConstructorAccessor get(Constructor<?> targetConstructor, ClassLoader classLoader);

    default FieldAccessor get(Field targetField) {
        return get(targetField, targetField.getClass().getClassLoader());
    }

    FieldAccessor get(Field targetField, ClassLoader classLoader);

}
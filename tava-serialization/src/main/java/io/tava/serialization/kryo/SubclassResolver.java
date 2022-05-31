package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.util.DefaultClassResolver;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-05-17 15:06
 */
public class SubclassResolver extends DefaultClassResolver {

    @Override
    public Registration getRegistration(Class type) {
        Registration registration = super.getRegistration(type);
        if (registration != null) {
            return registration;
        }
        registration = findRegistration(type.getSuperclass());
        if (registration != null) {
            classToRegistration.put(type, registration);
        }
        return registration;
    }

    private Registration findRegistration(Class<?> type) {
        if (type == null || type == Object.class) {
            return null;
        }
        Registration registration = super.getRegistration(type);
        if (registration != null) {
            return registration;
        }
        registration = this.findRegistration(type.getSuperclass());
        if (registration != null) {
            return registration;
        }
        Class<?>[] interfaces = type.getInterfaces();
        for (Class<?> clazz : interfaces) {
            registration = findRegistration(clazz);
            if (registration != null) {
                return registration;
            }
        }
        return null;
    }
}

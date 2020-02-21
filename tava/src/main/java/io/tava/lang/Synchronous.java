package io.tava.lang;

import io.tava.function.CheckedFunction0;

public interface Synchronous<T> extends CheckedFunction0<T> {

    @Override
    default T apply() throws Throwable {
        return get();
    }

    T get() throws Throwable;

}

package io.tava.lang;

import io.tava.function.CheckedFunction0;
import io.tava.function.Function1;

public final class Lazy<T> implements Asynchronous<T> {

    private final CheckedFunction0<T> action;

    public Lazy(CheckedFunction0<T> action) {
        this.action = action;
    }

    @Override
    public T get() throws Throwable {
        return action.apply();
    }

    public <R> Lazy<R> map(Function1<T, R> map) {
        return new Lazy<>(() -> map.apply(get()));
    }

    public static <T> Lazy<T> lazy(CheckedFunction0<T> action) {
        return new Lazy<>(action);
    }

}

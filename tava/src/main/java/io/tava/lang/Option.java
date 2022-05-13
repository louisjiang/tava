package io.tava.lang;

import io.tava.function.Consumer1;
import io.tava.function.Function0;
import io.tava.function.Function1;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

public interface Option<T> {

    T get();

    default T getOrNull() {
        return this.getOrElse((T) null);
    }

    default T getOrElse(Function0<T> defaultValue) {
        if (this.hasValue()) {
            return this.get();
        }
        if (defaultValue == null) {
            return null;
        }
        return defaultValue.apply();
    }

    default T getOrElse(T defaultValue) {
        if (this.hasValue()) {
            return this.get();
        }
        return defaultValue;
    }

    boolean isEmpty();

    boolean hasValue();

    Optional<T> toOptional();

    <R> Option<R> map(Function1<T, R> map);

    void forEach(Consumer1<T> foreach);

    static <V> Option.Some<V> some(V value) {
        return new Some<>(value);
    }

    static <V> Option.None<V> none() {
        return None.getInstance();
    }

    static <V> Option<V> option(V value) {
        if (value == null) {
            return none();
        }
        if (value instanceof String && ((String) value).isEmpty()) {
            return none();
        }

        if (value instanceof Collection && ((Collection<?>) value).isEmpty()) {
            return none();
        }

        if (value instanceof Map && ((Map<?, ?>) value).isEmpty()) {
            return none();
        }

        if (value instanceof Object[] && ((Object[]) value).length == 0) {
            return none();
        }

        if (value instanceof byte[] && ((byte[]) value).length == 0) {
            return none();
        }

        if (value instanceof short[] && ((short[]) value).length == 0) {
            return none();
        }

        if (value instanceof char[] && ((char[]) value).length == 0) {
            return none();
        }

        if (value instanceof int[] && ((int[]) value).length == 0) {
            return none();
        }
        if (value instanceof long[] && ((long[]) value).length == 0) {
            return none();
        }
        if (value instanceof float[] && ((float[]) value).length == 0) {
            return none();
        }
        if (value instanceof double[] && ((double[]) value).length == 0) {
            return none();
        }

        if (value instanceof boolean[] && ((boolean[]) value).length == 0) {
            return none();
        }

        return some(value);
    }

    class Some<T> implements Option<T> {

        private final T value;

        Some(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public <R> Option<R> map(Function1<T, R> map) {
            return Option.option(map.apply(get()));
        }

        @Override
        public void forEach(Consumer1<T> foreach) {
            foreach.accept(get());
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean hasValue() {
            return true;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.of(value);
        }
    }

    class None<T> implements Option<T> {

        private None() {
        }

        @Override
        public T get() {
            throw new NoSuchElementException("Option.None.get");
        }

        @Override
        @SuppressWarnings("unchecked")
        public <R> Option.None<R> map(Function1<T, R> map) {
            return (Option.None<R>) this;
        }

        @Override
        public void forEach(Consumer1<T> foreach) {

        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        @SuppressWarnings("unchecked")
        public static <T> None<T> getInstance() {
            return (None<T>) NoneHolder.none;
        }

        static class NoneHolder {
            static None<?> none = new None<>();
        }

    }

}

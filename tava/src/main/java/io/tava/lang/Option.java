package io.tava.lang;

import io.tava.function.Function1;

import java.util.NoSuchElementException;
import java.util.Optional;

public interface Option<T> {

    T get();

    boolean hasValue();

    Optional<T> toOptional();

    <R> Option<R> map(Function1<T, R> map);

    static <V> Option.Some<V> some(V value) {
        return new Some<>(value);
    }

    static <V> Option.None<V> none() {
        return None.getInstance();
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

        public <R> Option.Some<R> map(Function1<T, R> map) {
            return Option.some(map.apply(get()));
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

        public <R> Option.None<R> map(Function1<T, R> map) {
            return (Option.None<R>) this;
        }

        @Override
        public boolean hasValue() {
            return false;
        }

        @Override
        public Optional<T> toOptional() {
            return Optional.empty();
        }

        public static <T> None<T> getInstance() {
            return (None<T>) NoneHolder.none;
        }

        static class NoneHolder {
            static None<?> none = new None<>();
        }

    }

}

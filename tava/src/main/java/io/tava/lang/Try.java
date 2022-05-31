package io.tava.lang;

import io.tava.function.CheckedFunction0;
import io.tava.function.Function1;

import java.util.NoSuchElementException;

public interface Try<T> {

    boolean isSuccess();

    default boolean isFailure() {
        return !isSuccess();
    }

    T get();

    Option<T> toOption();

    <R> Try<R> map(Function1<T, R> map);

    Throwable getThrowable();

    <E extends Throwable> Try<T> rethrow() throws E;

    Either<Throwable, T> toEither();

    Success<T> recover(Function1<? super Throwable, ? extends T> op);

    static <T> Success<T> success(T value) {
        return new Success<>(value);
    }

    static <T> Failure<T> failure(Throwable cause) {
        return new Failure<>(cause);
    }

    static <T> Try<T> run(CheckedFunction0<T> action) {
        try {
            return Try.success(action.apply());
        } catch (Throwable cause) {
            return Try.failure(cause);
        }
    }

    class Success<T> implements Try<T> {

        private final T value;

        Success(T value) {
            this.value = value;
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public Option<T> toOption() {
            return Option.option(value);
        }

        @Override
        public <R> Try<R> map(Function1<T, R> map) {
            return Try.run(() -> map.apply(get()));
        }

        @Override
        public Throwable getThrowable() {
            throw new NoSuchElementException("Try.Success.getThrowable");
        }

        @Override
        public <E extends Throwable> Try<T> rethrow() throws E {
            return this;
        }

        @Override
        public Either<Throwable, T> toEither() {
            return Either.right(this.value);
        }

        @Override
        public Success<T> recover(Function1<? super Throwable, ? extends T> op) {
            return this;
        }
    }

    class Failure<T> implements Try<T> {

        private final Throwable cause;

        Failure(Throwable cause) {
            this.cause = cause;
        }

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public T get() {
            throw new NoSuchElementException("Try.Failure.get");
        }

        @Override
        public Option<T> toOption() {
            return Option.none();
        }

        @Override
        public <R> Try.Failure<R> map(Function1<T, R> map) {
            return (Failure<R>) this;
        }

        @Override
        public Throwable getThrowable() {
            return cause;
        }

        @Override
        public <E extends Throwable> Try<T> rethrow() throws E {
            throw (E) cause;
        }

        @Override
        public Either<Throwable, T> toEither() {
            return Either.left(cause);
        }

        @Override
        public Success<T> recover(Function1<? super Throwable, ? extends T> op) {
            return Try.success(op.apply(cause));
        }

    }

}

package io.tava.lang;

import java.util.NoSuchElementException;

public interface Either<L, R> {

    L left();

    R right();

    boolean isLeft();

    boolean isRight();

    static <L, R> Left<L, R> left(L value) {
        return new Left<>(value);
    }

    static <L, R> Right<L, R> right(R value) {
        return new Right<>(value);
    }

    class Left<L, R> implements Either<L, R> {

        private final L value;

        public Left(L value) {
            this.value = value;
        }

        @Override
        public L left() {
            return value;
        }

        @Override
        public R right() {
            throw new NoSuchElementException("Either.Left.right");
        }

        @Override
        public boolean isLeft() {
            return true;
        }

        @Override
        public boolean isRight() {
            return false;
        }

    }

    class Right<L, R> implements Either<L, R> {

        private final R value;

        public Right(R value) {
            this.value = value;
        }

        @Override
        public L left() {
            throw new NoSuchElementException("Either.Right.left");
        }

        @Override
        public R right() {
            return value;
        }

        @Override
        public boolean isLeft() {
            return false;
        }

        @Override
        public boolean isRight() {
            return true;
        }

    }


}

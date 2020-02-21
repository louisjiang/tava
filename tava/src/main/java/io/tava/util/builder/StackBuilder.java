package io.tava.util.builder;

import io.tava.util.Stack;

public final class StackBuilder<E> implements CollectionBuilder<E, Stack<E>> {

    private final Stack<E> list = new Stack<>();

    @Override
    public Stack<E> build() {
        return list;
    }

}
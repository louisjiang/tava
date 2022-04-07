package io.tava.util.builder;

import io.tava.util.LinkedList;

public final class LinkedListBuilder<E> implements CollectionBuilder<E, LinkedList<E>> {

    private final LinkedList<E> list = new LinkedList<>();

    @Override
    public LinkedList<E> build() {
        return list;
    }

}
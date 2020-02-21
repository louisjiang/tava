package io.tava.util.builder;

import io.tava.util.concurrent.CopyOnWriteArraySet;

public class CopyOnWriteArraySetBuilder<E> implements CollectionBuilder<E, CopyOnWriteArraySet<E>> {

    private final CopyOnWriteArraySet<E> list = new CopyOnWriteArraySet<E>();

    @Override
    public CopyOnWriteArraySet<E> build() {
        return list;
    }

}

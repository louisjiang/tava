package io.tava.util.builder;

import io.tava.util.concurrent.CopyOnWriteArrayList;

public final class CopyOnWriteArrayListBuilder<E> implements CollectionBuilder<E, CopyOnWriteArrayList<E>> {

    private final CopyOnWriteArrayList<E> list = new CopyOnWriteArrayList<E>();

    @Override
    public CopyOnWriteArrayList<E> build() {
        return list;
    }

}

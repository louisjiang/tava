package io.tava.util.builder;

import io.tava.util.Vector;

public final class VectorBuilder<E> implements CollectionBuilder<E, Vector<E>> {

    private final Vector<E> list = new Vector<>();

    @Override
    public Vector<E> build() {
        return list;
    }

}
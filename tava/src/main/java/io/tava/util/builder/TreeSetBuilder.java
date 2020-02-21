package io.tava.util.builder;

import io.tava.util.TreeSet;

public final class TreeSetBuilder<E> implements CollectionBuilder<E, TreeSet<E>> {

    private final TreeSet<E> set = new TreeSet<E>();

    @Override
    public TreeSet<E> build() {
        return set;
    }

}

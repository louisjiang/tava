package io.tava.util.builder;

import io.tava.util.HashSet;

public final class HashSetBuilder<E> implements CollectionBuilder<E, HashSet<E>> {

    private final HashSet<E> set = new HashSet<>();

    @Override
    public HashSet<E> build() {
        return set;
    }

}
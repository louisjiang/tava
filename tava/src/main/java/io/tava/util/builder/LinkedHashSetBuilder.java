package io.tava.util.builder;

import io.tava.util.LinkedHashSet;

public final class LinkedHashSetBuilder<E> implements CollectionBuilder<E, LinkedHashSet<E>> {

    private final LinkedHashSet<E> set = new LinkedHashSet<>();

    @Override
    public LinkedHashSet<E> build() {
        return set;
    }

}
package io.tava.util.builder;

import io.tava.util.concurrent.ConcurrentSkipListSet;

public final class ConcurrentSkipListSetBuilder<E> implements CollectionBuilder<E, ConcurrentSkipListSet<E>> {

    private final ConcurrentSkipListSet<E> set = new ConcurrentSkipListSet<E>();

    @Override
    public ConcurrentSkipListSet<E> build() {
        return set;
    }
    
}

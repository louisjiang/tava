package io.tava.util.builder;

import io.tava.util.Collection;

public interface CollectionBuilder<E, C extends Collection<E>> {

    default CollectionBuilder<E, C> add(E element) {
        build().add(element);
        return this;
    }

    default CollectionBuilder<E, C> add(java.util.Collection<? extends E> collection) {
        build().addAll(collection);
        return this;
    }

    C build();

}


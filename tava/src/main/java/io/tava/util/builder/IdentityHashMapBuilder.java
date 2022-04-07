package io.tava.util.builder;

import io.tava.util.IdentityHashMap;

public final class IdentityHashMapBuilder<K, V> implements MapBuilder<K, V, IdentityHashMap<K, V>> {

    private final IdentityHashMap<K, V> map = new IdentityHashMap<K, V>();

    @Override
    public IdentityHashMap<K, V> build() {
        return map;
    }
}

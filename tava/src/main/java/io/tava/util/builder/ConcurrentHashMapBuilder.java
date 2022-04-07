package io.tava.util.builder;

import io.tava.util.concurrent.ConcurrentHashMap;

public final class ConcurrentHashMapBuilder<K, V> implements MapBuilder<K, V, ConcurrentHashMap<K, V>> {

    private final ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();

    @Override
    public ConcurrentHashMap<K, V> build() {
        return map;
    }

}

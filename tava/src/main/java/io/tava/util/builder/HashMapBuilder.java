package io.tava.util.builder;

import io.tava.util.HashMap;

public final class HashMapBuilder<K, V> implements MapBuilder<K, V, HashMap<K, V>> {

    private final HashMap<K, V> map = new HashMap<>();

    @Override
    public HashMap<K, V> build() {
        return map;
    }
}

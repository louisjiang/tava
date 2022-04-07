package io.tava.util.builder;

import io.tava.util.LinkedHashMap;

public final class LinkedHashMapBuilder<K, V> implements MapBuilder<K, V, LinkedHashMap<K, V>> {

    private final LinkedHashMap<K, V> map = new LinkedHashMap<>();

    @Override
    public LinkedHashMap<K, V> build() {
        return map;
    }
}

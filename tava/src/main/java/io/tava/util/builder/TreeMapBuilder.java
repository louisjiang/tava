package io.tava.util.builder;

import io.tava.util.TreeMap;

public final class TreeMapBuilder<K, V> implements MapBuilder<K, V, TreeMap<K, V>> {

    private final TreeMap<K, V> map = new TreeMap<>();

    @Override
    public TreeMap<K, V> build() {
        return map;
    }
}

package io.tava.util.builder;

import io.tava.util.WeakHashMap;

public final class WeakHashMapBuilder<K, V> implements MapBuilder<K, V, WeakHashMap<K, V>> {

    private final WeakHashMap<K, V> map = new WeakHashMap<>();

    @Override
    public WeakHashMap<K, V> build() {
        return map;
    }
}

package io.tava.util.builder;

import io.tava.util.concurrent.ConcurrentSkipListMap;

public final class ConcurrentSkipListMapBuilder<K, V> implements MapBuilder<K, V, ConcurrentSkipListMap<K, V>> {

    private final ConcurrentSkipListMap<K, V> map = new ConcurrentSkipListMap<K, V>();

    @Override
    public ConcurrentSkipListMap<K, V> build() {
        return map;
    }

}

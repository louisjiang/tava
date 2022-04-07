package io.tava.util.builder;

import io.tava.util.Hashtable;

public final class HashtableBuilder<K, V> implements MapBuilder<K, V, Hashtable<K, V>> {

    private final Hashtable<K, V> map = new Hashtable<K, V>();

    @Override
    public Hashtable<K, V> build() {
        return map;
    }
}

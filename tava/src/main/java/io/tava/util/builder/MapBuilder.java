package io.tava.util.builder;

import io.tava.lang.Tuple2;
import io.tava.util.Map;

public interface MapBuilder<K, V, M extends Map<K, V>> {

    default MapBuilder<K, V, M> put(java.util.Map.Entry<K, V> entry) {
        return this.put(entry.getKey(), entry.getValue());
    }

    default MapBuilder<K, V, M> put(K key, V value) {
        build().put(key, value);
        return this;
    }

    default MapBuilder<K, V, M> put(Tuple2<K, V> tuple2) {
        build().put(tuple2.getValue1(), tuple2.getValue2());
        return this;
    }

    default MapBuilder<K, V, M> putAll(java.util.Map<? extends K, ? extends V> map) {
        build().putAll(map);
        return this;
    }

    M build();

}

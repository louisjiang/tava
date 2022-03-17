package io.tava.db.segment;


import io.tava.function.Consumer2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SegmentMap<K, V> extends Segment {

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    boolean containsValue(V value);

    V get(K key);

    V put(K key, V value);

    V remove(K key);

    void putAll(Map<? extends K, ? extends V> map);

    void clear();

    Set<K> keySet();

    Collection<V> values();

    Set<Map.Entry<K, V>> entrySet();

    void forEach(Consumer2<? super K, ? super V> action);

    Map<K, V> toMap();

    SegmentMap<K, V> remap(int segment);
}

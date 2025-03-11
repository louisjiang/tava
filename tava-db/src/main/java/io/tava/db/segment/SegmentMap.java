package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer1;
import io.tava.function.Consumer2;
import io.tava.function.Function1;
import io.tava.function.Function2;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SegmentMap<K, V> extends Segment {

    int size();

    boolean isEmpty();

    boolean containsKey(K key);

    boolean containsValue(V value);

    void foreach(K key, Consumer1<V> foreach);

    V update(K key, Function1<V, V> update);

    Map<K, V> update(Collection<K> keys, Function2<K, V, V> update);

    <T> T map(K key, Function1<V, T> function1);

    V get(K key);

    Map<K, V> get(Collection<K> keys);

    V put(K key, V value);

    V remove(K key);

    void removeAll(Collection<K> keys);

    void putAll(Map<? extends K, ? extends V> map);

    void clear();

    Set<K> keySet();

    Collection<V> values();

    Set<Map.Entry<K, V>> entrySet();

    Iterator<Map.Entry<K, V>> iterator();

    void forEach(Consumer2<? super K, ? super V> action);

    Map<K, V> toMap();

    boolean remap(int capacity);

    static <K, V> SegmentMap<K, V> get(Database database, String tableName, String key) {
        Map<String, Object> status = database.get(tableName + "@status", key);
        if (status == null) {
            return null;
        }
        return new SegmentHashMap<>(database, tableName, key, status);
    }

}

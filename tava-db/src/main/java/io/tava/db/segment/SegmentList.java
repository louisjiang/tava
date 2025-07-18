package io.tava.db.segment;

import io.tava.db.Database;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SegmentList<V> extends Segment {

    int size();

    boolean isEmpty();

    boolean contains(V value);

    Iterator<V> iterator();

    boolean add(V value);

    boolean remove(V value);

    boolean containsAll(Collection<? extends V> collection);

    boolean addAll(Collection<? extends V> collection);

    boolean addAll(int index, Collection<? extends V> collection);

    boolean removeAll(Collection<? extends V> collection);

    boolean retainAll(Collection<? extends V> collection);

    V get(int index);

    V set(int index, V value);

    void add(int index, V value);

    V remove(int index);

    int indexOf(V o);

    int lastIndexOf(V o);

    List<V> toList();

    SegmentList<V> relist(int capacity);

    static <V> SegmentList<V> get(Database database, String tableName, String key) {
        Map<String, Object> status = database.get(tableName + "@status", key);
        if (status == null) {
            return null;
        }
        return new SegmentArrayList<>(database, tableName, key, status);
    }

}

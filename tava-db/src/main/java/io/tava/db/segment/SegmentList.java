package io.tava.db.segment;

import io.tava.db.Database;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SegmentList<V> extends Segment {

    int size();

    boolean isEmpty();

    boolean contains(V o);

    Iterator<V> iterator();

    boolean add(V v);

    boolean remove(V o);

    boolean containsAll(Collection<? extends V> c);

    boolean addAll(Collection<? extends V> c);

    boolean addAll(int index, Collection<? extends V> c);

    boolean removeAll(Collection<? extends V> c);

    boolean retainAll(Collection<? extends V> c);

    void clear();

    V get(int index);

    V set(int index, V element);

    void add(int index, V element);

    V remove(int index);

    int indexOf(V o);

    int lastIndexOf(V o);

    List<V> toList();

    SegmentList<V> relist(int capacity);

    static <V> SegmentList<V> get(Database database, String tableName, String key) {
        Map<String, Object> status = database.get(tableName, key);
        if (status == null) {
            return null;
        }
        return new SegmentArrayList<>(database, tableName, key, status);
    }

}

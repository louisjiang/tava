package io.tava.db.segment;

import io.tava.db.Database;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface SegmentSet<V> extends Segment {

    int size();

    boolean isEmpty();

    boolean contains(V value);

    Iterator<V> iterator();

    boolean add(V value);

    boolean remove(V value);

    boolean containsAll(Collection<? extends V> collection);

    boolean addAll(Collection<? extends V> collection);

    boolean retainAll(Collection<? extends V> collection);

    boolean removeAll(Collection<? extends V> collection);

    void clear();

    Set<V> toSet();

    SegmentSet<V> reset(int capacity);

    static <V> SegmentSet<V> get(Database database, String tableName, String key) {
        Map<String, Object> status = database.get(tableName + "@status", key);
        if (status == null) {
            return null;
        }
        return new SegmentHashSet<>(database, tableName, key, status);
    }


}

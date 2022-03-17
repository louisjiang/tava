package io.tava.db.segment;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public interface SegmentSet<V> extends Segment {

    int size();

    boolean isEmpty();

    boolean contains(V o);

    Iterator<V> iterator();

    boolean add(V v);

    boolean remove(V o);

    boolean containsAll(Collection<? extends V> c);

    boolean addAll(Collection<? extends V> c);

    boolean retainAll(Collection<? extends V> c);

    boolean removeAll(Collection<? extends V> c);

    void clear();

    Set<V> toSet();

    SegmentSet<V> reset(int segment);

}

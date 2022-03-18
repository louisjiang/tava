package io.tava.db.segment;

import io.tava.db.Database;

import java.util.*;

public class SegmentHashSet<V> implements SegmentSet<V> {

    private final Database database;
    private final String tableName;
    private final String key;
    private final int segment;
    private int size = 0;

    public SegmentHashSet(Database database, String tableName, String key, int segment) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
        Map<String, Object> status = this.database.get(this.tableName, this.key);
        if (status == null) {
            this.segment = segment;
            updateStatus();
            return;
        }
        this.size = (Integer) status.get("size");
        this.segment = (Integer) status.get("segment");
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size() == 0;
    }

    @Override
    public boolean contains(V o) {
        Set<V> set = this.database.get(this.tableName, this.segmentKey(o));
        if (set == null) {
            return false;
        }
        return set.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {

            private int index = 0;
            private Iterator<V> iterator;

            @Override
            public boolean hasNext() {
                if (iterator == null || !iterator.hasNext()) {
                    if (index == segment) {
                        return false;
                    }
                    Set<V> set = database.get(tableName, segmentKey(index));
                    if (set == null) {
                        index++;
                        return hasNext();
                    }
                    iterator = set.iterator();
                    return iterator.hasNext();
                }
                return true;
            }

            @Override
            public V next() {
                return iterator.next();
            }
        };

    }

    @Override
    public boolean add(V v) {
        String segmentKey = this.segmentKey(v);
        Set<V> set = this.database.get(this.tableName, segmentKey);
        if (set == null) {
            set = new HashSet<>();
        }
        if (set.add(v)) {
            this.incrementSize();
            this.database.put(this.tableName, segmentKey, set);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(V o) {
        String segmentKey = this.segmentKey(o);
        Set<V> set = this.database.get(this.tableName, segmentKey);
        if (set == null) {
            return false;
        }
        if (set.remove(o)) {
            this.decrementSize();
            this.database.put(this.tableName, segmentKey, set);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<? extends V> c) {
        for (V v : c) {
            if (!contains(v)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        for (V v : c) {
            this.add(v);
        }
        return true;
    }

    @Override
    public boolean retainAll(Collection<? extends V> c) {
        Set<V> set = new HashSet<>();
        for (int i = 0; i < this.segment; i++) {
            Set<V> s = this.database.get(this.tableName, this.segmentKey(i));
            if (s == null) {
                continue;
            }
            if (!s.retainAll(c) || s.isEmpty()) {
                continue;
            }
            set.addAll(s);
        }
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.size = 0;
        if (set.isEmpty()) {
            updateStatus();
            return false;
        }
        addAll(set);
        return true;
    }

    @Override
    public boolean removeAll(Collection<? extends V> c) {
        boolean flag = false;
        for (V v : c) {
            flag |= remove(v);
        }
        return flag;
    }

    @Override
    public void clear() {
        this.size = 0;
        updateStatus();
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
    }

    @Override
    public Set<V> toSet() {
        Set<V> set = new HashSet<>();
        for (int i = 0; i < this.segment; i++) {
            Set<V> s = this.database.get(this.tableName, this.segmentKey(i));
            if (s != null) {
                set.addAll(s);
            }
        }
        return set;
    }

    @Override
    public SegmentSet<V> reset(int segment) {
        if (this.segment == segment) {
            return this;
        }
        Set<V> set = toSet();
        this.destroy();
        SegmentSet<V> segmentSet = new SegmentHashSet<>(this.database, this.tableName, this.key, segment);
        segmentSet.addAll(set);
        this.commit();
        return segmentSet;
    }

    @Override
    public void commit() {
        this.database.commit(this.tableName);
    }

    @Override
    public void destroy() {
        this.database.delete(this.tableName, this.key);
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, segmentKey(i));
        }
        this.commit();
    }

    private void incrementSize() {
        this.size++;
        updateStatus();
    }

    private void decrementSize() {
        this.size--;
        updateStatus();
    }

    private void updateStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("size", this.size);
        status.put("segment", this.segment);
        this.database.put(this.tableName, this.key, status);
    }

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private int indexFor(int h) {
        return h & (this.segment - 1);
    }

    private String segmentKey(Object key) {
        int value = indexFor(hash(key));
        return segmentKey(value);
    }

    private String segmentKey(int value) {
        return this.key + "@" + Math.abs(value);
    }

}

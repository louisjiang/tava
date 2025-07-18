package io.tava.db.segment;

import io.tava.db.Database;

import java.io.IOException;
import java.util.*;

public class SegmentHashSet<V> extends AbstractSegment implements SegmentSet<V> {

    private final long sequence;
    private final int segment;
    private int size;

    public SegmentHashSet(Database database, String tableName, String key, int segment) {
        this(database, tableName, key, segment, false);
    }

    protected SegmentHashSet(Database database, String tableName, String key, int segment, boolean initialize) {
        super(database, tableName, key);
        Map<String, Object> status;
        if (initialize || (status = this.database.get(this.tableName + "@status", this.key)) == null) {
            this.sequence = SnowFlakeUtil.nextId();
            this.segment = segment;
            this.size = 0;
            this.updateStatus();
            return;
        }
        this.sequence = (Long) status.get("sequence");
        this.segment = (Integer) status.get("segment");
        this.size = (Integer) status.get("size");
        this.statusData = status.get("status");
    }

    public SegmentHashSet(Database database, String tableName, String key, Map<String, Object> status) {
        super(database, tableName, key);
        this.sequence = (Long) status.get("sequence");
        this.segment = (Integer) status.get("segment");
        this.size = (Integer) status.get("size");
        this.statusData = status.get("status");
    }


    @Override
    public int segment() {
        return this.segment;
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
    public boolean contains(V value) {
        String segmentKey = this.segmentKey(value);
        Set<V> set = this.database.get(this.tableName, segmentKey);
        if (set == null) {
            return false;
        }
        return set.contains(value);
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<>() {

            private int index = 0;
            private java.util.Iterator<V> iterator;

            @Override
            public boolean hasNext() {
                if (this.iterator == null || !this.iterator.hasNext()) {
                    if (this.index == segment) {
                        return false;
                    }
                    Set<V> set = database.get(tableName, segmentKey(this.index));
                    index++;
                    if (set == null) {
                        return hasNext();
                    }
                    iterator = set.iterator();
                    return hasNext();
                }
                return true;
            }

            @Override
            public V next() {
                return iterator.next();
            }

            @Override
            public void close() throws IOException {
            }
        };

    }

    @Override
    public boolean add(V value) {
        String segmentKey = this.segmentKey(value);
        Set<V> set = this.database.get(this.tableName, segmentKey);
        if (set == null) {
            set = new HashSet<>();
        }
        if (set.add(value)) {
            this.incrementSize();
            this.database.put(this.tableName, segmentKey, set);
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(V value) {
        String segmentKey = this.segmentKey(value);
        Set<V> set = this.database.get(this.tableName, segmentKey);
        if (set == null) {
            return false;
        }
        if (set.remove(value)) {
            this.decrementSize();
            this.database.put(this.tableName, segmentKey, set);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<? extends V> collection) {
        for (V v : collection) {
            if (!contains(v)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends V> collection) {
        for (V v : collection) {
            this.add(v);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<? extends V> collection) {
        boolean remove = false;
        for (V v : collection) {
            remove |= remove(v);
        }
        return remove;
    }

    @Override
    public void clear() {
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.size = 0;
        this.updateStatusData(null);
        this.commit();
    }

    @Override
    public Set<V> toSet() {
        Set<V> set = new HashSet<>();
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            Set<V> s = this.database.get(this.tableName, segmentKey);
            if (s != null) {
                set.addAll(s);
            }
        }
        return set;
    }

    @Override
    public SegmentSet<V> reset(int capacity) {
        int newSegment = this.size / capacity;
        if (newSegment <= this.segment) {
            return this;
        }
        newSegment = this.segment * 2;
        return reinitialize(newSegment);
    }

    public SegmentSet<V> reinitialize(int newSegment) {
        if (this.segment == newSegment) {
            return this;
        }
        Object status = this.getStatusData();
        this.commit();
        SegmentSet<V> segmentSet = new SegmentHashSet<>(this.database, this.tableName, this.key, newSegment, true);
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            Set<V> set = this.database.get(this.tableName, segmentKey);
            if (set == null) {
                continue;
            }
            segmentSet.addAll(set);
        }
        segmentSet.updateStatusData(status);
        segmentSet.commit();
        this.clear();
        return segmentSet;
    }

    @Override
    public void destroy() {
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, segmentKey(i));
        }
        this.commit();
    }

    private void incrementSize() {
        this.size++;
        this.updateStatus();
    }

    private void decrementSize() {
        this.size--;
        this.updateStatus();
    }

    void updateStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("sequence", this.sequence);
        map.put("segment", this.segment);
        map.put("size", this.size);
        map.put("status", this.statusData);
        this.database.put(this.tableName + "@status", key, map);
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
        return this.key + "@" + sequence + "@" + Math.abs(value);
    }

}

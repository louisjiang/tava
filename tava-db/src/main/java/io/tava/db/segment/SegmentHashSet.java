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
        if (Integer.bitCount(segment) != 1) {
            throw new IllegalArgumentException("segment must be a power of 2");
        }
        Map<String, Object> status;
        if (initialize || (status = this.database.get(this.tableName, this.key)) == null) {
            this.sequence = SnowFlakeUtil.nextId();
            this.segment = segment;
            this.size = 0;
            this.updateStatus();
            return;
        }
        this.sequence = (Long) status.get("sequence");
        this.segment = (Integer) status.get("segment");
        this.size = (Integer) status.get("size");
        this.status = status.get("status");
    }

    public SegmentHashSet(Database database, String tableName, String key, Map<String, Object> status) {
        super(database, tableName, key);
        this.sequence = (Long) status.get("sequence");
        this.segment = (Integer) status.get("segment");
        this.size = (Integer) status.get("size");
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
        Set<V> set = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(value)));
        if (set == null) {
            return false;
        }
        return set.contains(value);
    }

    @Override
    public Iterator<V> iterator() {
        this.readWriteLock.readLock().lock();
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
                readWriteLock.readLock().unlock();
            }
        };

    }

    @Override
    public boolean add(V value) {
        return this.writeLock(() -> {
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
        });
    }

    @Override
    public boolean remove(V value) {
        return this.writeLock(() -> {
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
        });
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
    public boolean retainAll(Collection<? extends V> collection) {
        return this.writeLock(() -> {
            Set<V> set = new HashSet<>();
            for (int i = 0; i < this.segment; i++) {
                Set<V> s = this.database.get(this.tableName, this.segmentKey(i));
                if (s == null) {
                    continue;
                }
                if (!s.retainAll(collection) || s.isEmpty()) {
                    continue;
                }
                set.addAll(s);
            }
            for (int i = 0; i < this.segment; i++) {
                this.database.delete(this.tableName, this.segmentKey(i));
            }
            this.size = 0;
            if (set.isEmpty()) {
                this.updateStatus();
                return false;
            }
            this.addAll(set);
            return true;
        });
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
        this.writeLock(() -> {
            this.size = 0;
            this.updateStatus();
            for (int i = 0; i < this.segment; i++) {
                this.database.delete(this.tableName, this.segmentKey(i));
            }
        });
    }

    @Override
    public Set<V> toSet() {
        Set<V> set = new HashSet<>();
        for (int i = 0; i < this.segment; i++) {
            int index = i;
            Set<V> s = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(index)));
            if (s != null) {
                set.addAll(s);
            }
        }
        return set;
    }

    @Override
    public SegmentSet<V> reset(int capacity) {
        int segment = this.size / capacity;
        if (segment <= this.segment) {
            return this;
        }
        segment = this.segment * 2;
        return _reset_(segment);
    }

    private SegmentSet<V> _reset_(int segment) {
        if (this.segment == segment) {
            return this;
        }
        return this.writeLock(() -> {
            this.commit();
            SegmentSet<V> segmentSet = new SegmentHashSet<>(this.database, this.tableName, this.key, segment, true);
            for (int i = 0; i < this.segment; i++) {
                String segmentKey = this.segmentKey(i);
                Set<V> set = this.database.get(this.tableName, segmentKey);
                if (set == null) {
                    continue;
                }
                segmentSet.addAll(set);
                this.database.delete(this.tableName, segmentKey);
            }
            segmentSet.commit();
            this.database.updateSegmentCache(toString("set@", this.tableName, "@", this.key), segmentSet);
            return segmentSet;
        });
    }

    @Override
    public void destroy() {
        this.writeLock(() -> {
            this.database.delete(this.tableName, this.key);
            for (int i = 0; i < this.segment; i++) {
                this.database.delete(this.tableName, segmentKey(i));
            }
            this.commit();
        });
    }

    private void incrementSize() {
        this.size++;
        this.updateStatus();
    }

    private void decrementSize() {
        this.size--;
        this.updateStatus();
    }

    private void updateStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sequence", this.sequence);
        status.put("segment", this.segment);
        status.put("size", this.size);
        status.put("status", super.status);
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
        return this.key + "@" + sequence + "@" + Math.abs(value);
    }

}

package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer1;
import io.tava.function.Consumer2;
import io.tava.function.Function1;
import one.util.streamex.StreamEx;

import java.io.IOException;
import java.util.*;

public class SegmentHashMap<K, V> extends AbstractSegment implements SegmentMap<K, V> {

    private final long sequence;
    private final int segment;
    private int size;

    public SegmentHashMap(Database database, String tableName, String key, int segment) {
        this(database, tableName, key, segment, false);
    }

    protected SegmentHashMap(Database database, String tableName, String key, int segment, boolean initialize) {
        super(database, tableName, key);
        if (Integer.bitCount(segment) != 1) {
            throw new IllegalArgumentException("segment must be a power of 2");
        }
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
        this.status = status.get("status");
    }

    public SegmentHashMap(Database database, String tableName, String key, Map<String, Object> status) {
        super(database, tableName, key);
        this.sequence = (Long) status.get("sequence");
        this.segment = (Integer) status.get("segment");
        this.size = (Integer) status.get("size");
        this.status = status.get("status");
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
    public boolean containsKey(K key) {
        Map<K, V> map = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(key)));
        if (map == null) {
            return false;
        }
        return map.containsKey(key);
    }


    @Override
    public boolean containsValue(V value) {
        for (int i = 0; i < this.segment; i++) {
            int index = i;
            Map<K, V> map = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(index)));
            if (map == null) {
                continue;
            }
            if (map.containsValue(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void foreach(K key, Consumer1<V> foreach) {
        this.readLock(() -> {
            Map<K, V> map = this.database.get(this.tableName, this.segmentKey(key));
            if (map == null) {
                return;
            }
            V value = map.get(key);
            if (value == null) {
                return;
            }
            foreach.accept(value);
        });
    }

    @Override
    public void update(K key, Function1<V, V> update) {
        this.writeLock(() -> {
            this.put(key, update.apply(this.get(key)));
        });
    }

    @Override
    public <T> T map(K key, Function1<V, T> function1) {
        return this.readLock(() -> {
            Map<K, V> map = this.database.get(this.tableName, this.segmentKey(key));
            if (map == null) {
                return null;
            }
            V value = map.get(key);
            if (value == null) {
                return null;
            }
            return function1.apply(value);
        });
    }

    @Override
    public V get(K key) {
        Map<K, V> map = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(key)));
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public Map<K, V> get(Collection<K> keys) {
        Map<String, List<K>> groupedKeys = StreamEx.of(keys).groupingBy(this::segmentKey);
        Map<K, V> values = new HashMap<>();
        for (Map.Entry<String, List<K>> entry : groupedKeys.entrySet()) {
            String key = entry.getKey();
            this.readLock(() -> {
                Map<K, V> map = this.database.get(this.tableName, key);
                if (map == null) {
                    return;
                }
                for (K k : entry.getValue()) {
                    V v = map.get(k);
                    if (v == null) {
                        continue;
                    }
                    values.put(k, v);
                }
            });
        }
        return values;
    }

    @Override
    public V put(K key, V value) {
        return this.writeLock(() -> {
            String segmentKey = this.segmentKey(key);
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                map = new HashMap<>();
            }
            int size = map.size();
            map.put(key, value);
            if (map.size() - 1 == size) {
                this.incrementSize();
            }
            this.database.put(this.tableName, segmentKey, map);
            return value;
        });
    }

    @Override
    public V remove(K key) {
        return this.writeLock(() -> {
            String segmentKey = this.segmentKey(key);
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                return null;
            }
            int size = map.size();
            V v = map.remove(key);
            if (map.size() + 1 == size) {
                this.decrementSize();
                this.database.put(this.tableName, segmentKey, map);
            }
            return v;
        });
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
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
    public Set<K> keySet() {
        return toMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return toMap().values();
    }

    @Override
    public Set<Map.Entry<K, V>> entrySet() {
        return toMap().entrySet();
    }

    @Override
    public Iterator<Map.Entry<K, V>> iterator() {
        this.readWriteLock.readLock().lock();
        return new Iterator<>() {

            private int index = 0;
            private java.util.Iterator<Map.Entry<K, V>> iterator;

            @Override
            public boolean hasNext() {
                if (iterator == null || !iterator.hasNext()) {
                    if (index == segment) {
                        return false;
                    }
                    Map<K, V> map = database.get(tableName, segmentKey(index));
                    index++;
                    if (map == null) {
                        return hasNext();
                    }
                    iterator = map.entrySet().iterator();
                    return hasNext();
                }
                return true;
            }

            @Override
            public Map.Entry<K, V> next() {
                return iterator.next();
            }

            @Override
            public void close() throws IOException {
                readWriteLock.readLock().unlock();
            }
        };

    }

    @Override
    public void destroy() {
        this.writeLock(() -> {
//            this.database.delete(this.tableName + "@status", this.key);
            for (int i = 0; i < this.segment; i++) {
                this.database.delete(this.tableName, this.segmentKey(i));
            }
            this.commit();
        });
    }

    @Override
    public Map<K, V> toMap() {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < this.segment; i++) {
            int index = i;
            Map<K, V> m = this.readLock(() -> this.database.get(this.tableName, this.segmentKey(index)));
            if (m == null) {
                continue;
            }
            map.putAll(m);
        }
        return map;
    }

    @Override
    public SegmentMap<K, V> remap(int capacity) {
        int segment = this.size / capacity;
        if (segment <= this.segment) {
            return this;
        }
        segment = this.segment * 2;
        if (segment == this.segment) {
            return this;
        }
        return _remap_(segment);
    }

    private SegmentMap<K, V> _remap_(int segment) {
        if (segment == this.segment) {
            return this;
        }
        SegmentMap<K, V> segmentHashMap = this.writeLock(() -> {
            Object status = this.getStatus();
            this.commit();
            SegmentMap<K, V> segmentMap = new SegmentHashMap<>(this.database, this.tableName, this.key, segment, true);
            for (int i = 0; i < this.segment; i++) {
                String segmentKey = this.segmentKey(i);
                Map<K, V> map = this.database.get(this.tableName, segmentKey);
                if (map == null) {
                    continue;
                }
                segmentMap.putAll(map);
            }
            segmentMap.updateStatus(status);
            segmentMap.commit();
            this.database.updateSegmentCache(toString("map@", this.tableName, "@", this.key), segmentMap);
            return segmentMap;
        });
        this.destroy();
        return segmentHashMap;
    }

    public void forEach(Consumer2<? super K, ? super V> action) {
        for (int i = 0; i < this.segment; i++) {
            Map<K, V> map = this.database.get(this.tableName, this.segmentKey(i));
            if (map == null) {
                continue;
            }
            for (Map.Entry<K, V> entry : map.entrySet()) {
                action.accept(entry.getKey(), entry.getValue());
            }
        }
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
        map.put("status", this.status);
        this.database.put(this.tableName + "@status", this.key, map);
    }

    private int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private String segmentKey(Object key) {
        int value = this.indexFor(this.hash(key));
        return this.segmentKey(value);
    }

    private int indexFor(int h) {
        return h & (this.segment - 1);
    }

    private String segmentKey(int value) {
        return this.key + "@" + this.sequence + "@" + Math.abs(value);
    }

}

package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer1;
import io.tava.function.Consumer2;
import io.tava.function.Function1;
import io.tava.function.Function2;
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

    public SegmentHashMap(Database database, String tableName, String key, Map<String, Object> status) {
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
    public boolean containsKey(K key) {
        String segmentKey = this.segmentKey(key);
        Map<K, V> map = this.database.get(this.tableName, segmentKey);
        if (map == null) {
            return false;
        }
        return map.containsKey(key);
    }


    @Override
    public boolean containsValue(V value) {
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
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
        String segmentKey = this.segmentKey(key);
        Map<K, V> map = this.database.get(this.tableName, segmentKey);
        if (map == null) {
            return;
        }
        V value = map.get(key);
        if (value == null) {
            return;
        }
        foreach.accept(value);
    }

    @Override
    public void update(K key, Function1<V, V> update) {
        this.put(key, update.apply(this.get(key)));
    }

    @Override
    public void update(Collection<K> keys, Function2<K, V, V> update) {
        Map<String, List<K>> groupedKeys = StreamEx.of(keys).groupingBy(this::segmentKey);
        for (Map.Entry<String, List<K>> entry : groupedKeys.entrySet()) {
            String segmentKey = entry.getKey();
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                map = new HashMap<>();
            }
            int size = map.size();
            for (K key : entry.getValue()) {
                V value = update.apply(key, map.get(key));
                map.put(key, value);
            }
            this.incrementSize(map.size() - size);
            this.database.put(this.tableName, segmentKey, map);
        }
    }

    @Override
    public <T> T map(K key, Function1<V, T> function1) {
        String segmentKey = this.segmentKey(key);
        Map<K, V> map = this.database.get(this.tableName, segmentKey);
        if (map == null) {
            return null;
        }
        V value = map.get(key);
        if (value == null) {
            return null;
        }
        return function1.apply(value);
    }

    @Override
    public V get(K key) {
        String segmentKey = this.segmentKey(key);
        Map<K, V> map = this.database.get(this.tableName, segmentKey);
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public Map<K, V> get(Collection<K> keys) {
        Map<String, List<K>> groupedKeys = StreamEx.of(keys).groupingBy(this::segmentKey);
        Map<K, V> values = new HashMap<>(keys.size());
        for (Map.Entry<String, List<K>> entry : groupedKeys.entrySet()) {
            String segmentKey = entry.getKey();
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                continue;
            }
            for (K k : entry.getValue()) {
                V v = map.get(k);
                if (v == null) {
                    continue;
                }
                values.put(k, v);
            }
        }
        return values;
    }

    @Override
    public V put(K key, V value) {
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
    }


    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Map<String, Map<K, V>> segmentMap = new HashMap<>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            String segmentKey = segmentKey(key);
            Map<K, V> kvMap = segmentMap.computeIfAbsent(segmentKey, k -> new HashMap<>());
            kvMap.put(key, entry.getValue());
        }
        for (Map.Entry<String, Map<K, V>> entry : segmentMap.entrySet()) {
            String segmentKey = entry.getKey();
            Map<K, V> values = this.database.get(this.tableName, segmentKey);
            if (values == null) {
                values = new HashMap<>();
            }
            int size = values.size();
            values.putAll(entry.getValue());
            this.incrementSize(values.size() - size);
            this.database.put(this.tableName, segmentKey, values);
        }

    }

    @Override
    public V remove(K key) {
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
    }

    @Override
    public void removeAll(Collection<K> keys) {
        Map<String, List<K>> groupedKeys = StreamEx.of(keys).groupingBy(this::segmentKey);
        for (Map.Entry<String, List<K>> entry : groupedKeys.entrySet()) {
            String segmentKey = entry.getKey();
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                return;
            }
            int size = map.size();
            for (K k : entry.getValue()) {
                map.remove(k);
            }
            decrementSize(size - map.size());
            this.database.put(this.tableName, segmentKey, map);
        }
    }


    @Override
    public void clear() {
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            this.database.delete(this.tableName, segmentKey);
        }
        this.size = 0;
        this.updateStatusData(null);
        this.commit();
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
        return new Iterator<>() {

            private java.util.Iterator<Map.Entry<K, V>> iterator = null;
            private int index = 0;

            @Override
            public boolean hasNext() {
                if (iterator != null && iterator.hasNext()) {
                    return true;
                }

                while (this.index < segment) {
                    Map<K, V> map = database.get(tableName, segmentKey(index));
                    index++;
                    if (map == null) {
                        continue;
                    }
                    iterator = map.entrySet().iterator();
                    return hasNext();
                }
                return false;
            }

            @Override
            public Map.Entry<K, V> next() {
                return iterator.next();
            }

            @Override
            public void close() throws IOException {
            }
        };

    }

    @Override
    public void destroy() {
        for (int i = 0; i < this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.commit();
    }

    @Override
    public Map<K, V> toMap() {
        Map<K, V> map = new HashMap<>();
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            Map<K, V> m = this.database.get(this.tableName, segmentKey);
            if (m == null) {
                continue;
            }
            map.putAll(m);
        }
        return map;
    }

    @Override
    public SegmentMap<K, V> remap(int capacity) {
        int newSegment = this.size / capacity;
        if (newSegment <= this.segment) {
            return this;
        }
        newSegment = this.segment * 2;
        return reinitialize(newSegment);
    }

    public SegmentMap<K, V> reinitialize(int newSegment) {
        if (newSegment == this.segment) {
            return this;
        }
        Object status = this.getStatusData();
        this.commit();
        SegmentMap<K, V> segmentMap = new SegmentHashMap<>(this.database, this.tableName, this.key, newSegment, true);
        for (int i = 0; i < this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            Map<K, V> map = this.database.get(this.tableName, segmentKey);
            if (map == null) {
                continue;
            }
            segmentMap.putAll(map);
        }
        this.destroy();
        segmentMap.updateStatusData(status);
        segmentMap.commit();
        return segmentMap;
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

    private void incrementSize(int value) {
        this.size = this.size + value;
        this.updateStatus();
    }

    private void decrementSize() {
        this.size--;
        this.updateStatus();
    }

    private void decrementSize(int value) {
        this.size = this.size - value;
        this.updateStatus();
    }


    void updateStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("sequence", this.sequence);
        map.put("segment", this.segment);
        map.put("size", this.size);
        map.put("status", this.statusData);
        this.database.put(this.tableName + "@status", this.key, map);
    }

    private int hash(K key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    private String segmentKey(K key) {
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

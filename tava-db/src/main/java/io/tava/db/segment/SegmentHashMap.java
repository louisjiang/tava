package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer2;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

public class SegmentHashMap<K, V> implements SegmentMap<K, V> {

    private final Database database;
    private final String tableName;
    private final String key;
    private final long sequence;
    private final int segment;
    private int size;

    public SegmentHashMap(Database database, String tableName, String key, int segment) {
        this(database, tableName, key, segment, false);
    }

    protected SegmentHashMap(Database database, String tableName, String key, int segment, boolean initialize) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
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
    }

    public SegmentHashMap(Database database, String tableName, String key, Map<String, Object> status) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
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
    public boolean containsKey(K key) {
        Map<K, V> map = this.database.get(this.tableName, this.segmentKey(key));
        if (map == null) {
            return false;
        }
        return map.containsKey(key);
    }


    @Override
    public boolean containsValue(V value) {
        for (int i = 0; i < this.segment; i++) {
            Map<K, V> map = this.database.get(this.tableName, this.segmentKey(i));
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
    public V get(K key) {
        Map<K, V> map = this.database.get(this.tableName, this.segmentKey(key));
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        String segmentKey = this.segmentKey(key);
        return this.database.writeLock(this.tableName, () -> {
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
        String segmentKey = this.segmentKey(key);
        return this.database.writeLock(this.tableName, () -> {
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
        this.database.writeLock(this.tableName, () -> {
            for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
                put(entry.getKey(), entry.getValue());
            }
        });
    }

    @Override
    public void clear() {
        this.database.writeLock(this.tableName, () -> {
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
        final Lock readLock = this.database.readLock(this.tableName);
        readLock.lock();
        return new Iterator<Map.Entry<K, V>>() {

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
                readLock.unlock();
            }
        };

    }

    @Override
    public void commit() {
        this.database.commit(this.tableName);
    }

    @Override
    public void destroy() {
        this.database.writeLock(this.tableName, () -> {
            this.database.delete(this.tableName, this.key);
            for (int i = 0; i < this.segment; i++) {
                this.database.delete(this.tableName, this.segmentKey(i));
            }
            this.commit();
        });
    }

    @Override
    public Map<K, V> toMap() {
        return this.database.readLock(this.tableName, () -> {
            Map<K, V> map = new HashMap<>();
            for (int i = 0; i < this.segment; i++) {
                Map<K, V> m = this.database.get(this.tableName, this.segmentKey(i));
                if (m == null) {
                    continue;
                }
                map.putAll(m);
            }
            return map;
        });
    }

    @Override
    public SegmentMap<K, V> remap() {
        int segment = this.size / 1024;
        if (segment < this.segment) {
            return this;
        }
        int index = (int) Math.sqrt(this.segment) + 1;
        int newSegment;
        while ((newSegment = (int) Math.pow(2, index)) < segment) {
            index++;
        }
        return remap(newSegment);
    }

    @Override
    public SegmentMap<K, V> remap(int segment) {
        if (segment == this.segment) {
            return this;
        }
        return this.database.writeLock(this.tableName, () -> {
            SegmentMap<K, V> segmentMap = new SegmentHashMap<>(this.database, this.tableName, this.key, segment, true);
            for (int i = 0; i < this.segment; i++) {
                String segmentKey = this.segmentKey(i);
                Map<K, V> map = this.database.get(this.tableName, segmentKey);
                if (map == null) {
                    continue;
                }
                this.database.delete(this.tableName, segmentKey);
                segmentMap.putAll(map);
            }
            segmentMap.commit();
            return segmentMap;
        });
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


    private void updateStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sequence", this.sequence);
        status.put("segment", this.segment);
        status.put("size", this.size);
        this.database.put(this.tableName, this.key, status);
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

package io.tava.db.segment;

import io.tava.db.Database;

import java.util.*;

public class SegmentArrayList<V> implements SegmentList<V> {

    private final Database database;
    private final String tableName;
    private final String key;
    private final long sequence;
    private final int capacity;
    private int segment;
    private int size;

    public SegmentArrayList(Database database, String tableName, String key, int capacity) {
        this(database, tableName, key, capacity, false);
    }

    public SegmentArrayList(Database database, String tableName, String key, int capacity, boolean initialize) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;

        Map<String, Object> status;
        if (initialize || (status = this.database.get(this.tableName, this.key)) == null) {
            this.sequence = SnowFlakeUtil.nextId();
            this.capacity = capacity;
            this.size = 0;
            this.segment = 0;
            updateStatus();
            return;
        }
        this.sequence = (Long) status.get("sequence");
        this.capacity = (Integer) status.get("capacity");
        this.size = (Integer) status.get("size");
        this.segment = this.size / this.capacity;
    }

    public SegmentArrayList(Database database, String tableName, String key, Map<String, Object> status) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
        this.sequence = (Long) status.get("sequence");
        this.capacity = (Integer) status.get("capacity");
        this.size = (Integer) status.get("size");
        this.segment = this.size / this.capacity;
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
        for (int i = 0; i < this.segment; i++) {
            List<V> list = this.database.get(this.tableName, this.segmentKey(i));
            if (list == null) {
                break;
            }
            if (list.contains(o)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Iterator<V> iterator() {
        return new Iterator<V>() {

            private int index = 0;
            private java.util.Iterator<V> iterator;

            @Override
            public boolean hasNext() {
                if (iterator == null || !iterator.hasNext()) {
                    if (index > segment) {
                        return false;
                    }

                    List<V> list = database.get(tableName, segmentKey(index));
                    if (list == null) {
                        return false;
                    }
                    index++;
                    iterator = list.iterator();
                    return hasNext();
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
        String segmentKey = this.segmentKey();
        List<V> list = this.database.get(this.tableName, segmentKey);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(v);
        this.incrementSize();
        this.database.put(this.tableName, segmentKey, list);
        return true;
    }

    @Override
    public boolean remove(V o) {
        for (int i = 0; i <= this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            List<V> list = this.database.get(this.tableName, segmentKey);
            boolean remove = list.remove(o);
            if (!remove) {
                continue;
            }
            this.database.put(this.tableName, segmentKey, list);
            this.size = i * capacity + list.size();
            if (i == this.segment) {
                this.updateStatus();
                return true;
            }
            int index = i + 1;
            List<V> l = toList(index, this.segment);
            for (int j = index; j <= this.segment; j++) {
                this.database.delete(this.tableName, this.segmentKey(j));
            }
            this.addAll(l);
            return true;
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<? extends V> c) {
        for (V o : c) {
            if (!contains(o)) {
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
    public boolean addAll(int index, Collection<? extends V> c) {
        rangeCheckForAdd(index);
        int idx = index;
        int segment = idx / this.capacity;
        List<V> list = this.database.get(this.tableName, this.segmentKey(segment));
        if (list == null) {
            list = new ArrayList<>();
        }
        idx = idx % this.capacity;
        list.addAll(idx, c);
        List<V> values = null;
        if (segment < this.segment) {
            values = this.toList(segment + 1, this.segment);
            for (int i = segment; i <= this.segment; i++) {
                this.database.delete(this.tableName, this.segmentKey(i));
            }
        }
        this.size = segment * this.capacity;
        this.addAll(list);
        if (values != null) {
            this.addAll(values);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<? extends V> c) {
        boolean remove = false;
        for (V o : c) {
            remove = remove(o) || remove;
        }
        return remove;
    }

    @Override
    public boolean retainAll(Collection<? extends V> c) {
        List<V> list = new ArrayList<>();
        for (int i = 0; i <= this.segment; i++) {
            List<V> l = this.database.get(this.tableName, this.segmentKey(i));
            if (!l.retainAll(c) || l.isEmpty()) {
                continue;
            }
            list.addAll(l);
        }
        for (int i = 0; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.size = 0;
        if (list.isEmpty()) {
            updateStatus();
            return false;
        }
        addAll(list);
        return true;
    }

    @Override
    public void clear() {
        this.size = 0;
        this.updateStatus();
        for (int i = 0; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.segment = 0;
        this.commit();
    }

    @Override
    public V get(int index) {
        rangeCheck(index);
        int segment = index / this.capacity;
        List<V> list = this.database.get(this.tableName, this.segmentKey(segment));
        index = index % this.capacity;
        return list.get(index);
    }

    @Override
    public V set(int index, V element) {
        rangeCheck(index);
        String segmentKey = this.segmentKey(index / this.capacity);
        List<V> list = this.database.get(this.tableName, segmentKey);
        V v = list.set(index % this.capacity, element);
        this.database.put(this.tableName, segmentKey, list);
        return v;
    }

    @Override
    public void add(int index, V element) {
        this.addAll(index, Collections.singletonList(element));
    }

    @Override
    public V remove(int index) {
        rangeCheck(index);
        int segment = index / this.capacity;
        String segmentKey = this.segmentKey(segment);
        List<V> list = this.database.get(this.tableName, segmentKey);
        V v = list.remove(index % this.capacity);
        this.database.put(this.tableName, segmentKey, list);
        this.size = segment * this.capacity + list.size();
        if (segment == this.segment) {
            this.updateStatus();
            return v;
        }
        segment += 1;
        List<V> l = toList(segment, this.segment);
        for (int i = segment; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.addAll(l);
        return v;
    }

    @Override
    public int indexOf(V o) {
        for (int i = 0; i <= this.segment; i++) {
            List<V> list = this.database.get(this.tableName, this.segmentKey(i));
            int indexOf = list.indexOf(o);
            if (indexOf == -1) {
                continue;
            }
            return i * this.capacity + indexOf;
        }
        return -1;
    }

    @Override
    public int lastIndexOf(V o) {
        for (int i = this.segment; i >= 0; i--) {
            List<V> list = this.database.get(this.tableName, this.segmentKey(i));
            int lastIndexOf = list.lastIndexOf(o);
            if (lastIndexOf == -1) {
                continue;
            }
            return i * this.capacity + lastIndexOf;
        }
        return -1;
    }

    @Override
    public List<V> toList() {
        return toList(0, this.segment);
    }

    @Override
    public SegmentList<V> relist(int capacity) {
        if (this.capacity == capacity) {
            return this;
        }
        this.commit();
        SegmentList<V> segmentList = new SegmentArrayList<>(this.database, this.tableName, this.key, capacity, true);
        for (int i = 0; i <= this.segment; i++) {
            String segmentKey = this.segmentKey(i);
            List<V> l = this.database.get(this.tableName, segmentKey);
            if (l == null) {
                continue;
            }
            this.database.delete(this.tableName, segmentKey);
            segmentList.addAll(l);
        }
        segmentList.commit();
        return segmentList;
    }

    private List<V> toList(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        List<V> list = new ArrayList<>((end - start + 1) * this.capacity);
        for (int i = start; i <= end; i++) {
            List<V> l = this.database.get(this.tableName, this.segmentKey(i));
            if (l == null) {
                continue;
            }
            list.addAll(l);
        }
        return list;
    }

    @Override
    public void commit() {
        this.database.commit(this.tableName);
    }

    @Override
    public void destroy() {
        this.database.delete(this.tableName, this.key);
        for (int i = 0; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.segmentKey(i));
        }
        this.commit();
    }


    private String segmentKey() {
        this.segment = this.size / this.capacity;
        return this.segmentKey(this.segment);
    }

    private String segmentKey(int segment) {
        return this.key + "@" + this.sequence + "@" + segment;
    }

    private void incrementSize() {
        this.size++;
        this.updateStatus();
    }

    private void updateStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("sequence", this.sequence);
        status.put("size", this.size);
        status.put("capacity", this.capacity);
        this.database.put(this.tableName, this.key, status);
    }

    private void rangeCheck(int index) {
        if (index >= size)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private void rangeCheckForAdd(int index) {
        if (index > size || index < 0)
            throw new IndexOutOfBoundsException(outOfBoundsMsg(index));
    }

    private String outOfBoundsMsg(int index) {
        return "Index: " + index + ", Size: " + size;
    }


}

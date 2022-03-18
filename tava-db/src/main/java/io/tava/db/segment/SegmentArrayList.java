package io.tava.db.segment;

import io.tava.db.Database;

import java.util.*;

public class SegmentArrayList<V> implements SegmentList<V> {

    private final Database database;
    private final String tableName;
    private final String key;
    private final int capacity;
    private int segment = 0;
    private int size = 0;

    public SegmentArrayList(Database database, String tableName, String key, int capacity) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;

        Map<String, Object> status = this.database.get(this.tableName, this.key);
        if (status == null) {
            this.capacity = capacity;
            updateStatus();
            return;
        }
        this.size = (Integer) status.get("size");
        this.capacity = (Integer) status.get("capacity");
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
            List<V> list = this.database.get(this.tableName, this.key + "@" + i);
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
            private Iterator<V> iterator;

            @Override
            public boolean hasNext() {
                if (iterator == null || !iterator.hasNext()) {
                    if (index > segment) {
                        return false;
                    }

                    List<V> list = database.get(tableName, key + "@" + index);
                    if (list == null) {
                        return false;
                    }
                    index++;
                    iterator = list.iterator();
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
        String segmentKey = segmentKey();
        List<V> list = this.database.get(this.tableName, segmentKey);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(v);
        incrementSize();
        this.database.put(this.tableName, segmentKey, list);
        return true;
    }

    @Override
    public boolean remove(V o) {
        for (int i = 0; i <= this.segment; i++) {
            String key = this.key + "@" + i;
            List<V> list = this.database.get(this.tableName, key);
            boolean flag = list.remove(o);
            if (!flag) {
                continue;
            }
            this.database.put(this.tableName, key, list);
            this.size = i * capacity + list.size();
            if (i == this.segment) {
                updateStatus();
                return true;
            }
            int index = i + 1;
            List<V> l = toList(index, this.segment);
            for (int j = index; j <= this.segment; j++) {
                this.database.delete(this.tableName, this.key + "@" + j);
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
        int segment = index / this.capacity;
        List<V> list = this.database.get(this.tableName, this.key + "@" + segment);
        if (list == null) {
            list = new ArrayList<>();
        }
        index = index % this.capacity;
        list.addAll(index, c);
        List<V> values = null;
        if (segment < this.segment) {
            values = toList(segment + 1, this.segment);
            for (int i = segment; i <= this.segment; i++) {
                this.database.delete(this.tableName, this.key + "@" + i);
            }
        }
        this.size = segment * this.capacity;
        addAll(list);
        if (values != null) {
            addAll(values);
        }
        return true;
    }

    @Override
    public boolean removeAll(Collection<? extends V> c) {
        boolean flag = false;
        for (V o : c) {
            flag = remove(o) || flag;
        }
        return flag;
    }

    @Override
    public boolean retainAll(Collection<? extends V> c) {
        List<V> list = new ArrayList<>();
        for (int i = 0; i <= this.segment; i++) {
            List<V> l = this.database.get(this.tableName, this.key + "@" + i);
            if (!l.retainAll(c) || l.isEmpty()) {
                continue;
            }
            list.addAll(l);
        }
        for (int i = 0; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.key + "@" + i);
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
        updateStatus();
        for (int i = 0; i <= this.segment; i++) {
            this.database.delete(this.tableName, this.key + "@" + i);
        }
        this.segment = 0;
        this.commit();
    }

    @Override
    public V get(int index) {
        rangeCheck(index);
        int segment = index / this.capacity;
        List<V> list = this.database.get(this.tableName, this.key + "@" + segment);
        index = index % this.capacity;
        return list.get(index);
    }

    @Override
    public V set(int index, V element) {
        rangeCheck(index);
        int segment = index / this.capacity;
        List<V> list = this.database.get(this.tableName, this.key + "@" + segment);
        index = index % this.capacity;
        return list.set(index, element);
    }

    @Override
    public void add(int index, V element) {
        this.addAll(index, Collections.singletonList(element));
    }

    @Override
    public V remove(int index) {
        rangeCheck(index);
        int segment = index / this.capacity;
        String segmentKey = this.key + "@" + segment;
        List<V> list = this.database.get(this.tableName, segmentKey);
        index = index % this.capacity;
        V v = list.remove(index);
        this.database.put(this.tableName, segmentKey, list);
        this.size = segment * this.capacity + list.size();
        if (segment == this.segment) {
            return v;
        }
        segment += 1;
        List<V> l = toList(segment, this.segment);
        for (int j = segment; j <= this.segment; j++) {
            this.database.delete(this.tableName, this.key + "@" + j);
        }
        addAll(l);
        return v;
    }

    @Override
    public int indexOf(V o) {
        for (int i = 0; i <= this.segment; i++) {
            List<V> list = this.database.get(this.tableName, this.key + "@" + i);
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
            List<V> list = this.database.get(this.tableName, this.key + "@" + i);
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

        List<V> list = this.toList();
        this.destroy();
        SegmentList<V> segmentList = new SegmentArrayList<>(this.database, this.tableName, this.key, capacity);
        segmentList.addAll(list);
        this.commit();
        return segmentList;
    }

    private List<V> toList(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("start > end");
        }
        List<V> list = new ArrayList<>((end - start + 1) * this.capacity);
        for (int i = start; i <= end; i++) {
            List<V> l = this.database.get(this.tableName, this.key + "@" + i);
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
            this.database.delete(this.tableName, this.key + "@" + i);
        }
        this.commit();
    }


    private String segmentKey() {
        this.segment = this.size / this.capacity;
        return this.key + "@" + this.segment;
    }

    private void incrementSize() {
        this.size++;
        updateStatus();
    }

    private void updateStatus() {
        Map<String, Object> status = new HashMap<>();
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

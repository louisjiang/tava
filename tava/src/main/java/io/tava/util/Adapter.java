package io.tava.util;

import io.tava.util.builder.*;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-03 12:00:26
 */
public final class Adapter {

    private Adapter() {
    }

    public static <E> List<E> list(java.util.List<E> list) {
        return new ListAdapter<>(list);
    }

    public static <E> Set<E> set(java.util.Set<E> set) {
        return new SetAdapter<>(set);
    }

    public static <K, V> Map<K, V> map(java.util.Map<K, V> map) {
        return new MapAdapter<>(map);
    }

    private static class ListAdapter<E> extends AbstractList<E> implements List<E> {

        private java.util.List<E> list;

        ListAdapter(java.util.List<E> list) {
            this.list = list;
        }

        @Override
        public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
            return (CollectionBuilder<E0, C0>) new ArrayListBuilder<E0>();
        }

        @Override
        public E get(int index) {
            return list.get(index);
        }

        @Override
        public int size() {
            return list.size();
        }
    }

    private static class SetAdapter<E> extends AbstractSet<E> implements Set<E> {

        private final java.util.Set<E> set;

        SetAdapter(java.util.Set<E> set) {
            this.set = set;
        }

        @Override
        public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
            return (CollectionBuilder<E0, C0>) new HashSetBuilder<E0>();
        }

        @Override
        public Iterator<E> iterator() {
            return set.iterator();
        }

        @Override
        public int size() {
            return set.size();
        }
    }

    private static class MapAdapter<K, V> extends AbstractMap<K, V> implements Map<K, V> {

        private final java.util.Map<K, V> map;

        MapAdapter(java.util.Map<K, V> map) {
            this.map = map;
        }

        @Override
        public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
            return (MapBuilder<K0, V0, M0>) new HashMapBuilder<K, V>();
        }

        @Override
        public java.util.Set<Entry<K, V>> entrySet() {
            return map.entrySet();
        }
    }

}
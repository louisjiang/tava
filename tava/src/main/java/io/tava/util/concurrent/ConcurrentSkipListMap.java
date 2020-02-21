package io.tava.util.concurrent;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.Map;
import io.tava.util.MapOps;
import io.tava.util.builder.ConcurrentSkipListMapBuilder;
import io.tava.util.builder.MapBuilder;

import java.util.Comparator;
import java.util.SortedMap;

public class ConcurrentSkipListMap<K, V> extends java.util.concurrent.ConcurrentSkipListMap<K, V> implements ConcurrentNavigableMap<K, V> {

    public ConcurrentSkipListMap() {
        super();
    }

    public ConcurrentSkipListMap(Comparator<? super K> comparator) {
        super(comparator);
    }

    public ConcurrentSkipListMap(java.util.Map<? extends K, ? extends V> m) {
        super(m);
    }

    public ConcurrentSkipListMap(SortedMap<K, ? extends V> m) {
        super(m);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new ConcurrentSkipListMapBuilder<K0, V0>();
    }

    @Override
    public ConcurrentSkipListMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public ConcurrentSkipListMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    public ConcurrentSkipListMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public ConcurrentSkipListMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public ConcurrentSkipListMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public ConcurrentSkipListMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends ConcurrentSkipListMap<K, V>, ? extends ConcurrentSkipListMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends ConcurrentSkipListMap<K, V>, ? extends ConcurrentSkipListMap<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends ConcurrentSkipListMap<K, V>, ? extends ConcurrentSkipListMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> mapWithIndex(IndexedFunction1<Entry<K, V>, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> flatMap(Function1<Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0, V0> ConcurrentSkipListMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }


}

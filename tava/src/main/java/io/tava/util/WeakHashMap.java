package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.builder.MapBuilder;
import io.tava.util.builder.WeakHashMapBuilder;

public class WeakHashMap<K, V> extends java.util.WeakHashMap<K, V> implements Map<K, V> {

    public WeakHashMap() {
        super();
    }

    public WeakHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public WeakHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public WeakHashMap(java.util.Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new WeakHashMapBuilder<K0, V0>();
    }

    @Override
    public WeakHashMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public WeakHashMap<K, V> filter(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public WeakHashMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public WeakHashMap<K, V> filterNot(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public WeakHashMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public WeakHashMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public WeakHashMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public WeakHashMap<K, V> takeWhile(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public WeakHashMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public WeakHashMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public WeakHashMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public WeakHashMap<K, V> dropWhile(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public WeakHashMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends WeakHashMap<K, V>, ? extends WeakHashMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends WeakHashMap<K, V>, ? extends WeakHashMap<K, V>> span(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends WeakHashMap<K, V>, ? extends WeakHashMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> map(Function1<java.util.Map.Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> mapWithIndex(IndexedFunction1<java.util.Map.Entry<K, V>, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> flatMap(Function1<java.util.Map.Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0, V0> WeakHashMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0> WeakHashMap<K0, ? extends WeakHashMap<K, V>> groupBy(Function2<K, V, K0> action) {
        return (WeakHashMap<K0, ? extends WeakHashMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    public <K0> WeakHashMap<K0, ? extends WeakHashMap<K, V>> groupBy(Function1<java.util.Map.Entry<K, V>, K0> action) {
        return (WeakHashMap<K0, ? extends WeakHashMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    public <K0, R> WeakHashMap<K0, List<R>> groupMap(Function1<java.util.Map.Entry<K, V>, K0> action, Function1<java.util.Map.Entry<K, V>, R> mapAction) {
        return (WeakHashMap<K0, List<R>>) MapOps.groupMap(this, action, mapAction);
    }
}

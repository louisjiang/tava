package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.builder.LinkedHashMapBuilder;
import io.tava.util.builder.MapBuilder;

public class LinkedHashMap<K, V> extends java.util.LinkedHashMap<K, V> implements Map<K, V> {

    public LinkedHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedHashMap(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedHashMap() {
        super();
    }

    public LinkedHashMap(java.util.Map<? extends K, ? extends V> m) {
        super(m);
    }

    public LinkedHashMap(int initialCapacity,
                         float loadFactor,
                         boolean accessOrder) {
        super(initialCapacity, loadFactor, accessOrder);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new LinkedHashMapBuilder<K0, V0>();
    }

    @Override
    public LinkedHashMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public LinkedHashMap<K, V> filter(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public LinkedHashMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public LinkedHashMap<K, V> filterNot(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public LinkedHashMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public LinkedHashMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public LinkedHashMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public LinkedHashMap<K, V> takeWhile(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public LinkedHashMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public LinkedHashMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public LinkedHashMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public LinkedHashMap<K, V> dropWhile(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public LinkedHashMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends LinkedHashMap<K, V>, ? extends LinkedHashMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends LinkedHashMap<K, V>, ? extends LinkedHashMap<K, V>> span(Predicate1<java.util.Map.Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends LinkedHashMap<K, V>, ? extends LinkedHashMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> map(Function1<java.util.Map.Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> mapWithIndex(IndexedFunction1<java.util.Map.Entry<K, V>, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> flatMap(Function1<java.util.Map.Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0, V0> LinkedHashMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0> LinkedHashMap<K0, ? extends LinkedHashMap<K, V>> groupBy(Function2<K, V, K0> action) {
        return (LinkedHashMap<K0, ? extends LinkedHashMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    public <K0> LinkedHashMap<K0, ? extends LinkedHashMap<K, V>> groupBy(Function1<java.util.Map.Entry<K, V>, K0> action) {
        return (LinkedHashMap<K0, ? extends LinkedHashMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    public <K0, R> LinkedHashMap<K0, List<R>> groupMap(Function1<java.util.Map.Entry<K, V>, K0> action, Function1<java.util.Map.Entry<K, V>, R> mapAction) {
        return (LinkedHashMap<K0, List<R>>) MapOps.groupMap(this, action, mapAction);
    }
}

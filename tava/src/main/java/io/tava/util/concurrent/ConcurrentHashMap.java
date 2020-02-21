package io.tava.util.concurrent;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.Map;
import io.tava.util.MapOps;
import io.tava.util.builder.ConcurrentHashMapBuilder;
import io.tava.util.builder.MapBuilder;

public class ConcurrentHashMap<K, V> extends java.util.concurrent.ConcurrentHashMap<K, V> implements ConcurrentMap<K, V> {

    public ConcurrentHashMap() {
        super();
    }

    public ConcurrentHashMap(int initialCapacity) {
        super(initialCapacity);
    }


    public ConcurrentHashMap(java.util.Map<? extends K, ? extends V> m) {
        super(m);
    }


    public ConcurrentHashMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public ConcurrentHashMap(int initialCapacity, float loadFactor, int concurrencyLevel) {
        super(initialCapacity, loadFactor, concurrencyLevel);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new ConcurrentHashMapBuilder<K0, V0>();
    }

    @Override
    public ConcurrentHashMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public ConcurrentHashMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public ConcurrentHashMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public ConcurrentHashMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public ConcurrentHashMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public ConcurrentHashMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends ConcurrentHashMap<K, V>, ? extends ConcurrentHashMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends ConcurrentHashMap<K, V>, ? extends ConcurrentHashMap<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends ConcurrentHashMap<K, V>, ? extends ConcurrentHashMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> mapWithIndex(IndexedFunction1<java.util.Map.Entry<K, V>, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, java.util.Map.Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> flatMap(Function1<java.util.Map.Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0, V0> ConcurrentHashMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

}

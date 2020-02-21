package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.builder.IdentityHashMapBuilder;
import io.tava.util.builder.MapBuilder;

public class IdentityHashMap<K, V> extends java.util.IdentityHashMap<K, V> implements Map<K, V> {

    public IdentityHashMap() {
        super();
    }

    public IdentityHashMap(int expectedMaxSize) {
        super(expectedMaxSize);
    }

    public IdentityHashMap(java.util.Map<? extends K, ? extends V> m) {
        super(m);
    }

    @Override
    public <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder() {
        return (MapBuilder<K0, V0, M0>) new IdentityHashMapBuilder<K0, V0>();
    }

    @Override
    public IdentityHashMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public IdentityHashMap<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    public IdentityHashMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public IdentityHashMap<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    public IdentityHashMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    public IdentityHashMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    public IdentityHashMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public IdentityHashMap<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    public IdentityHashMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    public IdentityHashMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    public IdentityHashMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public IdentityHashMap<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    public IdentityHashMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    public Tuple2<? extends IdentityHashMap<K, V>, ? extends IdentityHashMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends IdentityHashMap<K, V>, ? extends IdentityHashMap<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    public Tuple2<? extends IdentityHashMap<K, V>, ? extends IdentityHashMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> mapWithIndex(IndexedFunction1<Entry<K, V>, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> flatMap(Function1<Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    public <K0, V0> IdentityHashMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

}

package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Tuple2;

public interface SortedMap<K, V> extends java.util.SortedMap<K, V>, Map<K, V> {

    @Override
    default SortedMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    default SortedMap<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    default SortedMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default SortedMap<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default SortedMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    default SortedMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    default SortedMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default SortedMap<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default SortedMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    default SortedMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    default SortedMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default SortedMap<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default SortedMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    default Tuple2<? extends SortedMap<K, V>, ? extends SortedMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends SortedMap<K, V>, ? extends SortedMap<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends SortedMap<K, V>, ? extends SortedMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> mapWithIndex(IndexedFunction1<Entry<K, V>, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> flatMap(Function1<Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    default <K0, V0> SortedMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    default <K0> SortedMap<K0, ? extends SortedMap<K, V>> groupBy(Function2<K, V, K0> action) {
        return (SortedMap<K0, ? extends SortedMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    default <K0> SortedMap<K0, ? extends SortedMap<K, V>> groupBy(Function1<Entry<K, V>, K0> action) {
        return (SortedMap<K0, ? extends SortedMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    default <K0, R> SortedMap<K0, List<R>> groupMap(Function1<Entry<K, V>, K0> action, Function1<Entry<K, V>, R> mapAction) {
        return (SortedMap<K0, List<R>>) MapOps.groupMap(this, action, mapAction);
    }

}

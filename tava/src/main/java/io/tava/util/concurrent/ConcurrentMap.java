package io.tava.util.concurrent;

import io.tava.function.*;
import io.tava.lang.Tuple2;
import io.tava.util.List;
import io.tava.util.Map;
import io.tava.util.MapOps;

public interface ConcurrentMap<K, V> extends java.util.concurrent.ConcurrentMap<K, V>, Map<K, V> {

    @Override
    default ConcurrentMap<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    default ConcurrentMap<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    @Override
    default ConcurrentMap<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default ConcurrentMap<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default ConcurrentMap<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    default ConcurrentMap<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    @Override
    default ConcurrentMap<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default ConcurrentMap<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default ConcurrentMap<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    default ConcurrentMap<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    @Override
    default ConcurrentMap<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default ConcurrentMap<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default ConcurrentMap<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    @Override
    default Tuple2<? extends ConcurrentMap<K, V>, ? extends ConcurrentMap<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends ConcurrentMap<K, V>, ? extends ConcurrentMap<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends ConcurrentMap<K, V>, ? extends ConcurrentMap<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> mapWithIndex(IndexedFunction1<Entry<K, V>, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> mapWithIndex(IndexedFunction2<K, V, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> flatMap(Function1<Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    default <K0, V0> ConcurrentMap<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    default <K0> ConcurrentMap<K0, ? extends ConcurrentMap<K, V>> groupBy(Function2<K, V, K0> action) {
        return (ConcurrentMap<K0, ? extends ConcurrentMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    default <K0> ConcurrentMap<K0, ? extends ConcurrentMap<K, V>> groupBy(Function1<Entry<K, V>, K0> action) {
        return (ConcurrentMap<K0, ? extends ConcurrentMap<K, V>>) MapOps.groupBy(this, action);
    }

    @Override
    default <K0, R> ConcurrentMap<K0, List<R>> groupMap(Function1<Entry<K, V>, K0> action, Function1<Entry<K, V>, R> mapAction) {
        return (ConcurrentMap<K0, List<R>>) MapOps.groupMap(this, action, mapAction);
    }
}

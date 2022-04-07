package io.tava.util;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;
import io.tava.util.builder.MapBuilder;
import one.util.streamex.StreamEx;

import java.util.Comparator;
import java.util.Set;


public interface Map<K, V> extends java.util.Map<K, V>, Traversable<java.util.Map.Entry<K, V>> {

    <K0, V0, M0 extends Map<K0, V0>> MapBuilder<K0, V0, M0> builder();

    @Override
    default StreamEx<Entry<K, V>> streamEx() {
        return StreamEx.of(entrySet());
    }

    @Override
    default Observable<Entry<K, V>> observable() {
        return Observable.fromIterable(entrySet());
    }

    @Override
    default Flowable<Entry<K, V>> flowable() {
        return Flowable.fromIterable(entrySet());
    }

    default void foreach(Consumer2<K, V> action) {
        foreach(entry -> action.accept(entry.getKey(), entry.getValue()));
    }

    @Override
    default void foreach(Consumer1<Entry<K, V>> action) {
        java.util.Set<Entry<K, V>> entries = entrySet();
        for (Entry<K, V> entry : entries) {
            action.accept(entry);
        }
    }

    default void foreachWithIndex(IndexedConsumer2<K, V> action) {
        java.util.Set<Entry<K, V>> entries = entrySet();
        int index = 0;
        for (Entry<K, V> entry : entries) {
            action.accept(index++, entry.getKey(), entry.getValue());
        }
    }

    @Override
    default void foreachWithIndex(IndexedConsumer1<Entry<K, V>> action) {
        java.util.Set<Entry<K, V>> entries = entrySet();
        int index = 0;
        for (Entry<K, V> entry : entries) {
            action.accept(index++, entry);
        }
    }

    default Map<K, V> filter(Predicate2<K, V> action) {
        return MapOps.filter(this, action);
    }

    @Override
    default Map<K, V> filter(Predicate1<Entry<K, V>> action) {
        return MapOps.filter(this, action);
    }

    default Map<K, V> filterNot(Predicate2<K, V> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default Map<K, V> filterNot(Predicate1<Entry<K, V>> action) {
        return MapOps.filterNot(this, action);
    }

    @Override
    default Map<K, V> take(int n) {
        return MapOps.take(this, n);
    }

    @Override
    default Map<K, V> takeRight(int n) {
        return MapOps.takeRight(this, n);
    }

    default Map<K, V> takeWhile(Predicate2<K, V> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default Map<K, V> takeWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.takeWhile(this, action);
    }

    @Override
    default Map<K, V> drop(int n) {
        return MapOps.drop(this, n);
    }

    @Override
    default Map<K, V> dropRight(int n) {
        return MapOps.dropRight(this, n);
    }

    default Map<K, V> dropWhile(Predicate2<K, V> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default Map<K, V> dropWhile(Predicate1<Entry<K, V>> action) {
        return MapOps.dropWhile(this, action);
    }

    @Override
    default Map<K, V> slice(int from, int until) {
        return MapOps.slice(this, from, until);
    }

    default Tuple2<? extends Map<K, V>, ? extends Map<K, V>> span(Predicate2<K, V> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends Map<K, V>, ? extends Map<K, V>> span(Predicate1<Entry<K, V>> action) {
        return MapOps.span(this, action);
    }

    @Override
    default Tuple2<? extends Map<K, V>, ? extends Map<K, V>> splitAt(int n) {
        return MapOps.splitAt(this, n);
    }

    default <K0, V0> Map<K0, V0> map(Function1<Entry<K, V>, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    default <K0, V0> Map<K0, V0> map(Function2<K, V, Tuple2<K0, V0>> action) {
        return MapOps.map(this, action);
    }

    default <K0, V0> Map<K0, V0> mapWithIndex(IndexedFunction1<Entry<K, V>, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    default <K0, V0> Map<K0, V0> mapWithIndex(IndexedFunction2<K, V, Entry<K0, V0>> action) {
        return MapOps.mapWithIndex(this, action);
    }

    default <K0, V0> Map<K0, V0> flatMap(Function1<Entry<K, V>, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    default <K0, V0> Map<K0, V0> flatMap(Function2<K, V, Map<K0, V0>> action) {
        return MapOps.flatMap(this, action);
    }

    @Override
    default List<Tuple2<Entry<K, V>, Integer>> zipWithIndex() {
        return MapOps.zipWithIndex(this);
    }

    @Override
    default <B> List<Tuple2<Entry<K, V>, B>> zip(Collection<B> that) {
        return MapOps.zip(this, that);
    }

    @Override
    default <A, B> Tuple2<List<A>, List<B>> unzip(Function1<Entry<K, V>, Tuple2<A, B>> action) {
        return MapOps.unzip(this, action);
    }

    default boolean forall(Predicate2<K, V> action) {
        return MapOps.forall(this, action);
    }

    @Override
    default boolean forall(Predicate1<Entry<K, V>> action) {
        return MapOps.forall(this, action);
    }

    default boolean exists(Predicate2<K, V> action) {
        return MapOps.exists(this, action);
    }

    @Override
    default boolean exists(Predicate1<Entry<K, V>> action) {
        return MapOps.exists(this, action);
    }

    default int count(Predicate2<K, V> action) {
        return MapOps.count(this, action);
    }

    @Override
    default int count(Predicate1<Entry<K, V>> action) {
        return MapOps.count(this, action);
    }

    default Option<Entry<K, V>> find(Predicate2<K, V> action) {
        return MapOps.find(this, action);
    }

    @Override
    default Option<Entry<K, V>> find(Predicate1<Entry<K, V>> action) {
        return MapOps.find(this, action);
    }

    @Override
    default <R> R foldLeft(R zero, Function2<R, Entry<K, V>, R> action) {
        return MapOps.foldLeft(this, zero, action);
    }

    @Override
    default <R> R foldLeftWithIndex(R zero, IndexedFunction2<R, Entry<K, V>, R> action) {
        return MapOps.foldLeftWithIndex(this, zero, action);
    }

    @Override
    default Entry<K, V> reduceLeft(Function2<? super Entry<K, V>, ? super Entry<K, V>, ? extends Entry<K, V>> action) {
        return MapOps.reduceLeft(this, action);
    }

    default <K0> Map<K0, ? extends Map<K, V>> groupBy(Function2<K, V, K0> action) {
        return MapOps.groupBy(this, action);
    }

    @Override
    default <K0> Map<K0, ? extends Map<K, V>> groupBy(Function1<Entry<K, V>, K0> action) {
        return MapOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, List<R>> groupMap(Function1<Entry<K, V>, K0> action, Function1<Entry<K, V>, R> mapAction) {
        return MapOps.groupMap(this, action, mapAction);
    }

    @Override
    default LinkedList<Entry<K, V>> reverse() {
        LinkedList<Entry<K, V>> list = new LinkedList<>();
        Set<Entry<K, V>> entries = entrySet();
        for (Entry<K, V> entry : entries) {
            list.addFirst(entry);
        }
        return list;
    }

    @Override
    default Entry<K, V> min(Comparator<? super Entry<K, V>> comparator) {
        return null;
    }

    @Override
    default Entry<K, V> max(Comparator<? super Entry<K, V>> comparator) {
        return null;
    }

    default <R> Set<R> toSet(Function2<K, V, R> action) {
        return toSet(entry -> action.apply(entry.getKey(), entry.getValue()));
    }

    default <R> Set<R> toSet(Function1<Entry<K, V>, R> action) {
        Set<R> set = new HashSet<>();
        Set<Entry<K, V>> entries = entrySet();
        for (Entry<K, V> entry : entries) {
            set.add(action.apply(entry));
        }
        return set;
    }

    default <R> List<R> toList(Function2<K, V, R> action) {
        return toList(entry -> action.apply(entry.getKey(), entry.getValue()));
    }

    default <R> List<R> toList(Function1<Entry<K, V>, R> action) {
        List<R> list = new ArrayList<>();
        Set<Entry<K, V>> entries = entrySet();
        for (Entry<K, V> entry : entries) {
            list.add(action.apply(entry));
        }
        return list;
    }


}

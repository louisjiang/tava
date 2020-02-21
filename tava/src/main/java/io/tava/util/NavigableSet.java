package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;

public interface NavigableSet<E> extends java.util.NavigableSet<E>, SortedSet<E> {

    @Override
    default NavigableSet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    default NavigableSet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    default NavigableSet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    default NavigableSet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    default NavigableSet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    default NavigableSet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    default NavigableSet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    default NavigableSet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    default NavigableSet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    default Tuple2<? extends NavigableSet<E>, ? extends NavigableSet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    default Tuple2<? extends NavigableSet<E>, ? extends NavigableSet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    default <R> NavigableSet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    default <R> NavigableSet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    default <R> NavigableSet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    default NavigableSet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    default <K0> Map<K0, ? extends NavigableSet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, ? extends NavigableSet<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    default NavigableSet<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    default NavigableSet<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

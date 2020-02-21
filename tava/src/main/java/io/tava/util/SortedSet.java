package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;


public interface SortedSet<E> extends java.util.SortedSet<E>, Set<E> {

    @Override
    default SortedSet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    default SortedSet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    default SortedSet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    default SortedSet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    default SortedSet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    default SortedSet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    default SortedSet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    default SortedSet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    default SortedSet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    default Tuple2<? extends SortedSet<E>, ? extends SortedSet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    default Tuple2<? extends SortedSet<E>, ? extends SortedSet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    default <R> SortedSet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    default <R> SortedSet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    default <R> SortedSet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    default SortedSet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    default <B> SortedSet<Tuple2<E, B>> zip(Collection<B> that) {
        return CollectionOps.zip(this, that);
    }

    @Override
    default <K0> Map<K0, ? extends SortedSet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, ? extends SortedSet<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    default SortedSet<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    default SortedSet<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;


public interface Set<E> extends java.util.Set<E>, Collection<E> {

    @Override
    default Set<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    default Set<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    default Set<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    default Set<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    default Set<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    default Set<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    default Set<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    default Set<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    default Set<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    default Tuple2<? extends Set<E>, ? extends Set<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    default Tuple2<? extends Set<E>, ? extends Set<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    default <R> Set<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    default <R> Set<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    default <R> Set<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    default Set<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    default <B> Set<Tuple2<E, B>> zip(Collection<B> that) {
        return CollectionOps.zip(this, that);
    }

    @Override
    default <K0> Map<K0, ? extends Set<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, ? extends Set<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    default Set<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    default Set<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

    default List<E> toList() {
        return new ArrayList<>(this);
    }

}

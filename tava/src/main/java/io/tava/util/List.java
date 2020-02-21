package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.lang.Tuple2;
import io.tava.function.Predicate1;


public interface List<E> extends Collection<E>, java.util.List<E> {

    @Override
    default List<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    default List<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    default List<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    default List<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    default List<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    default List<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    default List<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    default List<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    default List<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    default <R> List<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    default <R> List<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    default <R> List<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    default List<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    default Tuple2<? extends List<E>, ? extends List<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    default Tuple2<? extends List<E>, ? extends List<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    default <K> Map<K, ? extends List<E>> groupBy(Function1<E, K> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, ? extends List<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    default List<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    default List<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

    default List<E> reverse() {
        return CollectionOps.reverse(this);
    }

    default Set<E> toSet() {
        return new HashSet<>(this);
    }
}

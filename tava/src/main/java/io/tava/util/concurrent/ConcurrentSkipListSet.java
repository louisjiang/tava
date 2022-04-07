package io.tava.util.concurrent;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.Collection;
import io.tava.util.CollectionOps;
import io.tava.util.Map;
import io.tava.util.NavigableSet;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.ConcurrentSkipListSetBuilder;

import java.util.Comparator;
import java.util.SortedSet;

public class ConcurrentSkipListSet<E> extends java.util.concurrent.ConcurrentSkipListSet<E> implements NavigableSet<E> {

    public ConcurrentSkipListSet() {
        super();
    }

    public ConcurrentSkipListSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public ConcurrentSkipListSet(java.util.Collection<? extends E> c) {
        super(c);
    }

    public ConcurrentSkipListSet(SortedSet<E> s) {
        super(s);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new ConcurrentSkipListSetBuilder<E0>();
    }

    @Override
    public ConcurrentSkipListSet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public ConcurrentSkipListSet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public ConcurrentSkipListSet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public ConcurrentSkipListSet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public ConcurrentSkipListSet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public ConcurrentSkipListSet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public ConcurrentSkipListSet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public ConcurrentSkipListSet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public ConcurrentSkipListSet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> ConcurrentSkipListSet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> ConcurrentSkipListSet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> ConcurrentSkipListSet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public ConcurrentSkipListSet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public <B> ConcurrentSkipListSet<Tuple2<E, B>> zip(Collection<B> that) {
        return CollectionOps.zip(this, that);
    }

    @Override
    public Tuple2<ConcurrentSkipListSet<E>, ConcurrentSkipListSet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    public Tuple2<ConcurrentSkipListSet<E>, ConcurrentSkipListSet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public <K0> Map<K0, ConcurrentSkipListSet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, ConcurrentSkipListSet<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public ConcurrentSkipListSet<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public ConcurrentSkipListSet<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

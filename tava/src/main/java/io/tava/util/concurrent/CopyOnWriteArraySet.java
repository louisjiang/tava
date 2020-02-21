package io.tava.util.concurrent;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.*;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.CopyOnWriteArraySetBuilder;

public class CopyOnWriteArraySet<E> extends java.util.concurrent.CopyOnWriteArraySet<E> implements Set<E> {

    public CopyOnWriteArraySet() {
        super();
    }

    public CopyOnWriteArraySet(java.util.Collection<? extends E> c) {
        super(c);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new CopyOnWriteArraySetBuilder<E0>();
    }

    @Override
    public CopyOnWriteArraySet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public CopyOnWriteArraySet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public CopyOnWriteArraySet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public CopyOnWriteArraySet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public CopyOnWriteArraySet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public CopyOnWriteArraySet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public CopyOnWriteArraySet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public CopyOnWriteArraySet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public CopyOnWriteArraySet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> CopyOnWriteArraySet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> CopyOnWriteArraySet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> CopyOnWriteArraySet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public CopyOnWriteArraySet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public <B> CopyOnWriteArraySet<Tuple2<E, B>> zip(Collection<B> that) {
        return CollectionOps.zip(this, that);
    }

    @Override
    public Tuple2<CopyOnWriteArraySet<E>, CopyOnWriteArraySet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<CopyOnWriteArraySet<E>, CopyOnWriteArraySet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public <K0> Map<K0, CopyOnWriteArraySet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, CopyOnWriteArraySet<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public CopyOnWriteArraySet<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public CopyOnWriteArraySet<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

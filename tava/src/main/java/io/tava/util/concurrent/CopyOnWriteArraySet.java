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
    public Tuple2<? extends CopyOnWriteArraySet<E>, ? extends CopyOnWriteArraySet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<? extends CopyOnWriteArraySet<E>, ? extends CopyOnWriteArraySet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }


    @Override
    public <K0> Map<K0, ? extends CopyOnWriteArraySet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }
}

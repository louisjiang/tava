package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.LinkedHashSetBuilder;

public class LinkedHashSet<E> extends java.util.LinkedHashSet<E> implements Set<E> {

    public LinkedHashSet() {
        super();
    }

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    public LinkedHashSet(java.util.Collection<? extends E> c) {
        super(c);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new LinkedHashSetBuilder<E0>();
    }

    @Override
    public LinkedHashSet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public LinkedHashSet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public LinkedHashSet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public LinkedHashSet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public LinkedHashSet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public LinkedHashSet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public LinkedHashSet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public LinkedHashSet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public LinkedHashSet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> LinkedHashSet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> LinkedHashSet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> LinkedHashSet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public LinkedHashSet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public <B> LinkedHashSet<Tuple2<E, B>> zip(Collection<B> that) {
        return CollectionOps.zip(this, that);
    }

    @Override
    public Tuple2<LinkedHashSet<E>, LinkedHashSet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    public Tuple2<LinkedHashSet<E>, LinkedHashSet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public <K0> Map<K0, LinkedHashSet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, LinkedHashSet<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public LinkedHashSet<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public LinkedHashSet<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

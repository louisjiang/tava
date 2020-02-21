package io.tava.util.concurrent;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.Collection;
import io.tava.util.CollectionOps;
import io.tava.util.List;
import io.tava.util.Map;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.CopyOnWriteArrayListBuilder;

public class CopyOnWriteArrayList<E> extends java.util.concurrent.CopyOnWriteArrayList<E> implements List<E> {

    public CopyOnWriteArrayList() {
        super();
    }

    public CopyOnWriteArrayList(java.util.Collection<? extends E> c) {
        super(c);
    }

    public CopyOnWriteArrayList(E[] toCopyIn) {
        super(toCopyIn);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new CopyOnWriteArrayListBuilder<E0>();
    }

    @Override
    public CopyOnWriteArrayList<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public CopyOnWriteArrayList<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public CopyOnWriteArrayList<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public CopyOnWriteArrayList<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public CopyOnWriteArrayList<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public CopyOnWriteArrayList<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public CopyOnWriteArrayList<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public CopyOnWriteArrayList<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public CopyOnWriteArrayList<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> CopyOnWriteArrayList<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> CopyOnWriteArrayList<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> CopyOnWriteArrayList<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public CopyOnWriteArrayList<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }


    @Override
    public Tuple2<CopyOnWriteArrayList<E>, CopyOnWriteArrayList<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<CopyOnWriteArrayList<E>, CopyOnWriteArrayList<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public CopyOnWriteArrayList<E> reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K0> Map<K0, CopyOnWriteArrayList<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, CopyOnWriteArrayList<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public CopyOnWriteArrayList<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public CopyOnWriteArrayList<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

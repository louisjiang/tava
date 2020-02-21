package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.lang.Tuple2;
import io.tava.util.builder.VectorBuilder;
import io.tava.function.Predicate1;
import io.tava.util.builder.CollectionBuilder;

public class Vector<E> extends java.util.Vector<E> implements List<E> {

    public Vector() {
        super();
    }

    public Vector(int initialCapacity, int capacityIncrement) {
        super(initialCapacity, capacityIncrement);
    }

    public Vector(int initialCapacity) {
        super(initialCapacity);
    }

    public Vector(java.util.Collection<? extends E> c) {
        super(c);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new VectorBuilder<E0>();
    }

    @Override
    public Vector<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public Vector<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public Vector<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public Vector<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public Vector<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public Vector<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public Vector<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public Vector<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public Vector<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> Vector<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> Vector<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> Vector<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public Vector<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }


    @Override
    public Tuple2<? extends Vector<E>, ? extends Vector<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    public Tuple2<? extends Vector<E>, ? extends Vector<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public Vector<E> reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K0> Map<K0, ? extends Vector<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, ? extends Vector<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public Vector<E> diff(Collection<E> that) {
        return CollectionOps.diff(this,that);
    }

    @Override
    public Vector<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this,that);
    }

}

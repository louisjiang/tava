package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.builder.ArrayListBuilder;
import io.tava.util.builder.CollectionBuilder;

public class ArrayList<E> extends java.util.ArrayList<E> implements List<E> {

    public ArrayList() {
        super();
    }

    public ArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public ArrayList(java.util.Collection<? extends E> c) {
        super(c);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new ArrayListBuilder<E0>();
    }

    @Override
    public ArrayList<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public ArrayList<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public ArrayList<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public ArrayList<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public ArrayList<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public ArrayList<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public ArrayList<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public ArrayList<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public ArrayList<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> ArrayList<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> ArrayList<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> ArrayList<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public ArrayList<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public Tuple2<ArrayList<E>, ArrayList<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    public Tuple2<ArrayList<E>, ArrayList<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public ArrayList<E> reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K0> Map<K0, ArrayList<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    public <K0, R> Map<K0, ArrayList<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    public ArrayList<E> diff(Collection<E> that) {
        return CollectionOps.diff(this, that);
    }

    @Override
    public ArrayList<E> intersect(Collection<E> that) {
        return CollectionOps.intersect(this, that);
    }

}

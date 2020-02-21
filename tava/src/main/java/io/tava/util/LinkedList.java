package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.lang.Tuple2;
import io.tava.function.Predicate1;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.LinkedListBuilder;

public class LinkedList<E> extends java.util.LinkedList<E> implements List<E> {

    public LinkedList() {
        super();
    }

    public LinkedList(java.util.Collection<? extends E> c) {
        super(c);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new LinkedListBuilder<E0>();
    }

    @Override
    public LinkedList<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public LinkedList<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public LinkedList<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public LinkedList<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public LinkedList<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public LinkedList<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public LinkedList<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public LinkedList<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public LinkedList<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> LinkedList<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> LinkedList<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> LinkedList<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public LinkedList<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public Tuple2<? extends LinkedList<E>, ? extends LinkedList<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<? extends LinkedList<E>, ? extends LinkedList<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    public LinkedList<E> reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K0> Map<K0, ? extends LinkedList<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }
}

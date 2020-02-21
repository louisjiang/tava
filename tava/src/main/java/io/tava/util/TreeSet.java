package io.tava.util;

import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;
import io.tava.util.builder.TreeSetBuilder;

import java.util.Comparator;

public class TreeSet<E> extends java.util.TreeSet<E> implements NavigableSet<E> {

    public TreeSet() {
        super();
    }

    public TreeSet(Comparator<? super E> comparator) {
        super(comparator);
    }

    public TreeSet(java.util.Collection<? extends E> c) {
        super(c);
    }

    public TreeSet(java.util.SortedSet<E> s) {
        super(s);
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new TreeSetBuilder<E0>();
    }

    @Override
    public TreeSet<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public TreeSet<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public TreeSet<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public TreeSet<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public TreeSet<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public TreeSet<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public TreeSet<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public TreeSet<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public TreeSet<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> TreeSet<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> TreeSet<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> TreeSet<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public TreeSet<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }

    @Override
    public Tuple2<? extends TreeSet<E>, ? extends TreeSet<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    public Tuple2<? extends TreeSet<E>, ? extends TreeSet<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public <K0> Map<K0, ? extends TreeSet<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }
}

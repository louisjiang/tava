package io.tava.util;

import io.tava.util.builder.StackBuilder;
import io.tava.function.Function1;
import io.tava.function.IndexedFunction1;
import io.tava.function.Predicate1;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;

public class Stack<E> extends java.util.Stack<E> implements List<E> {

    public Stack() {
        super();
    }

    @Override
    public <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder() {
        return (CollectionBuilder<E0, C0>) new StackBuilder<E0>();
    }

    @Override
    public Stack<E> filter(Predicate1<E> action) {
        return CollectionOps.filter(this, action);
    }

    @Override
    public Stack<E> filterNot(Predicate1<E> action) {
        return CollectionOps.filterNot(this, action);
    }

    @Override
    public Stack<E> take(int n) {
        return CollectionOps.take(this, n);
    }

    @Override
    public Stack<E> takeRight(int n) {
        return CollectionOps.takeRight(this, n);
    }

    @Override
    public Stack<E> takeWhile(Predicate1<E> action) {
        return CollectionOps.takeWhile(this, action);
    }

    @Override
    public Stack<E> drop(int n) {
        return CollectionOps.drop(this, n);
    }

    @Override
    public Stack<E> dropRight(int n) {
        return CollectionOps.dropRight(this, n);
    }

    @Override
    public Stack<E> dropWhile(Predicate1<E> action) {
        return CollectionOps.dropWhile(this, action);
    }

    @Override
    public Stack<E> slice(int from, int until) {
        return CollectionOps.slice(this, from, until);
    }

    @Override
    public <R> Stack<R> map(Function1<E, R> action) {
        return CollectionOps.map(this, action);
    }

    @Override
    public <R> Stack<R> mapWithIndex(IndexedFunction1<E, R> action) {
        return CollectionOps.mapWithIndex(this, action);
    }

    @Override
    public <R> Stack<R> flatMap(Function1<E, Collection<R>> action) {
        return CollectionOps.flatMap(this, action);
    }

    @Override
    public Stack<Tuple2<E, Integer>> zipWithIndex() {
        return CollectionOps.zipWithIndex(this);
    }


    @Override
    public Tuple2<? extends Stack<E>, ? extends Stack<E>> span(Predicate1<E> action) {
        return CollectionOps.span(this, action);
    }

    @Override
    public Tuple2<? extends Stack<E>, ? extends Stack<E>> splitAt(int n) {
        return CollectionOps.splitAt(this, n);
    }

    @Override
    public Stack<E> reverse() {
        return CollectionOps.reverse(this);
    }

    @Override
    public <K0> Map<K0, ? extends Stack<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }
}

package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;

import java.util.Comparator;

public interface Collection<E> extends java.util.Collection<E>, Traversable<E> {

    <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder();

    @Override
    default void foreach(Consumer1<E> action) {
        for (E e : this) {
            action.accept(e);
        }
    }

    @Override
    default void foreachWithIndex(IndexedConsumer1<E> action) {
        int index = 0;
        for (E e : this) {
            action.accept(index++, e);
        }
    }

    @Override
    Collection<E> filter(Predicate1<E> action);

    @Override
    Collection<E> filterNot(Predicate1<E> action);

    @Override
    Collection<E> take(int n);

    @Override
    Collection<E> takeRight(int n);

    @Override
    Collection<E> takeWhile(Predicate1<E> action);

    @Override
    Collection<E> drop(int n);

    @Override
    Collection<E> dropRight(int n);

    @Override
    Collection<E> dropWhile(Predicate1<E> action);

    @Override
    Collection<E> slice(int from, int until);

    @Override
    Tuple2<? extends Collection<E>, ? extends Collection<E>> span(Predicate1<E> action);

    @Override
    Tuple2<? extends Collection<E>, ? extends Collection<E>> splitAt(int n);

    default <K, V> Map<K, V> toMap(Function1<E, Tuple2<K, V>> action) {
        return CollectionOps.toMap(this, action);
    }

    <R> Collection<R> map(Function1<E, R> action);

    <R> Collection<R> mapWithIndex(IndexedFunction1<E, R> action);

    <R> Collection<R> flatMap(Function1<E, Collection<R>> action);

    @Override
    Collection<Tuple2<E, Integer>> zipWithIndex();

    @Override
    <B> Collection<Tuple2<E, B>> zip(Collection<B> that);

    @Override
    default boolean forall(Predicate1<E> action) {
        return CollectionOps.forall(this, action);
    }

    @Override
    default boolean exists(Predicate1<E> action) {
        return CollectionOps.exists(this, action);
    }

    @Override
    default int count(Predicate1<E> action) {
        return CollectionOps.count(this, action);
    }

    @Override
    default Option<E> find(Predicate1<E> action) {
        return CollectionOps.find(this, action);
    }

    @Override
    default <R> R foldLeft(R zero, Function2<R, E, R> action) {
        return CollectionOps.foldLeft(this, zero, action);
    }

    @Override
    default <R> R foldLeftWithIndex(R zero, IndexedFunction2<R, E, R> action) {
        return CollectionOps.foldLeftWithIndex(this, zero, action);
    }

    @Override
    default E reduceLeft(Function2<? super E, ? super E, ? extends E> action) {
        return CollectionOps.reduceLeft(this, action);
    }

    @Override
    <K0> Map<K0, ? extends Collection<E>> groupBy(Function1<E, K0> action);

    @Override
    <K0, R> Map<K0, ? extends Collection<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction);

    @Override
    default Collection<E> reverse() {
        LinkedList<E> list = new LinkedList<>();
        for (E e : this) {
            list.addFirst(e);
        }
        return list;
    }

    Collection<E> diff(Collection<E> that);

    Collection<E> intersect(Collection<E> that);

    @Override
    default E min(Comparator<? super E> comparator) {
        return CollectionOps.min(this, comparator);
    }

    @Override
    default E max(Comparator<? super E> comparator) {
        return CollectionOps.max(this, comparator);
    }

}

package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;
import io.tava.util.builder.CollectionBuilder;

public interface Collection<E> extends java.util.Collection<E>, Traversable<E> {

    <E0, C0 extends Collection<E0>> CollectionBuilder<E0, C0> builder();

    default void foreach(Consumer1<E> action) {
        for (E e : this) {
            action.accept(e);
        }
    }

    default void foreachWithIndex(IndexedConsumer1<E> action) {
        int index = 0;
        for (E e : this) {
            action.accept(index++, e);
        }
    }

    Collection<E> filter(Predicate1<E> action);

    Collection<E> filterNot(Predicate1<E> action);

    Collection<E> take(int n);

    Collection<E> takeRight(int n);

    Collection<E> takeWhile(Predicate1<E> action);

    Collection<E> drop(int n);

    Collection<E> dropRight(int n);

    Collection<E> dropWhile(Predicate1<E> action);

    Collection<E> slice(int from, int until);

    Tuple2<? extends Collection<E>, ? extends Collection<E>> span(Predicate1<E> action);

    Tuple2<? extends Collection<E>, ? extends Collection<E>> splitAt(int n);

    default <K, V> Map<K, V> toMap(Function1<E, Tuple2<K, V>> action) {
        return CollectionOps.toMap(this, action);
    }

    <R> Collection<R> map(Function1<E, R> action);

    <R> Collection<R> mapWithIndex(IndexedFunction1<E, R> action);

    <R> Collection<R> flatMap(Function1<E, Collection<R>> action);

    Collection<Tuple2<E, Integer>> zipWithIndex();

    default boolean forall(Predicate1<E> action) {
        return CollectionOps.forall(this, action);
    }

    default boolean exists(Predicate1<E> action) {
        return CollectionOps.exists(this, action);
    }

    default int count(Predicate1<E> action) {
        return CollectionOps.count(this, action);
    }

    default Option<E> find(Predicate1<E> action) {
        return CollectionOps.find(this, action);
    }

    default <R> R foldLeft(R zero, Function2<R, E, R> action) {
        return CollectionOps.foldLeft(this, zero, action);
    }

    default <R> R foldLeftWithIndex(R zero, IndexedFunction2<R, E, R> action) {
        return CollectionOps.foldLeftWithIndex(this, zero, action);
    }

    default <R> R foldRight(R zero, Function2<R, E, R> action) {
        return reverse().foldLeft(zero, action);
    }

    default <R> R foldRightWithIndex(R zero, IndexedFunction2<R, E, R> action) {
        return reverse().foldLeftWithIndex(zero, action);
    }

    @Override
    default E reduceLeft(Function2<? super E, ? super E, ? extends E> action) {
        return CollectionOps.reduceLeft(this, action);
    }

    @Override
    default <K0> Map<K0, ? extends Collection<E>> groupBy(Function1<E, K0> action) {
        return CollectionOps.groupBy(this, action);
    }

    @Override
    default <K0, R> Map<K0, ? extends Collection<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction) {
        return CollectionOps.groupMap(this, action, mapAction);
    }

    @Override
    default Collection<E> reverse() {
        LinkedList<E> list = new LinkedList<>();
        for (E e : this) {
            list.addFirst(e);
        }
        return list;
    }

}

package io.tava.util;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Observable;
import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;
import one.util.streamex.StreamEx;

import java.util.Comparator;

public interface Traversable<E> {

    StreamEx<E> streamEx();

    Observable<E> observable();

    Flowable<E> flowable();

    default void println() {
        foreach(System.out::println);
    }

    void foreach(Consumer1<E> action);

    void foreachWithIndex(IndexedConsumer1<E> action);

    boolean isEmpty();

    Traversable<E> filter(Predicate1<E> action);

    Traversable<E> filterNot(Predicate1<E> action);

    Traversable<E> take(int n);

    Traversable<E> takeRight(int n);

    Traversable<E> takeWhile(Predicate1<E> action);

    Traversable<E> drop(int n);

    Traversable<E> dropRight(int n);

    Traversable<E> dropWhile(Predicate1<E> action);

    Traversable<E> slice(int from, int until);

    Tuple2<? extends Traversable<E>, ? extends Traversable<E>> span(Predicate1<E> action);

    Tuple2<? extends Traversable<E>, ? extends Traversable<E>> splitAt(int n);

    Traversable<Tuple2<E, Integer>> zipWithIndex();

    <B> Traversable<Tuple2<E, B>> zip(Collection<B> that);

    <A, B> Tuple2<? extends Traversable<A>, ? extends Traversable<B>> unzip(Function1<E, Tuple2<A, B>> action);

    boolean forall(Predicate1<E> action);

    boolean exists(Predicate1<E> action);

    int count(Predicate1<E> action);

    Option<E> find(Predicate1<E> action);

    default <R> R fold(R zero, Function2<R, E, R> action) {
        return foldLeft(zero, action);
    }

    <R> R foldLeft(R zero, Function2<R, E, R> action);

    <R> R foldLeftWithIndex(R zero, IndexedFunction2<R, E, R> action);

    default <R> R foldRightWithIndex(R zero, IndexedFunction2<R, E, R> action) {
        return reverse().foldLeftWithIndex(zero, action);
    }

    default <R> R foldRight(R zero, Function2<R, E, R> action) {
        return reverse().foldLeft(zero, action);
    }

    default E reduce(Function2<? super E, ? super E, ? extends E> action) {
        return reduceLeft(action);
    }

    default Option<E> reduceOption(Function2<? super E, ? super E, ? extends E> action) {
        return reduceLeftOption(action);
    }

    default Option<E> reduceLeftOption(Function2<? super E, ? super E, ? extends E> action) {
        E e = reduceLeft(action);
        if (e == null) {
            return Option.some(e);
        }
        return Option.none();
    }

    E reduceLeft(Function2<? super E, ? super E, ? extends E> action);

    default Option<E> reduceRightOption(Function2<? super E, ? super E, ? extends E> action) {
        E e = reduceRight(action);
        if (e == null) {
            return Option.none();
        }
        return Option.some(e);
    }

    default E reduceRight(Function2<? super E, ? super E, ? extends E> action) {
        return reverse().reduceLeft(action);
    }

    <K0> Map<K0, ? extends Traversable<E>> groupBy(Function1<E, K0> action);

    <K0, R> Map<K0, ? extends Traversable<R>> groupMap(Function1<E, K0> action, Function1<E, R> mapAction);

    default Option<E> minOption(Comparator<? super E> comparator) {
        if (isEmpty()) {
            return Option.none();
        }
        return Option.some(min(comparator));
    }

    E min(Comparator<? super E> comparator);

    default Option<E> maxOption(Comparator<? super E> comparator) {
        if (isEmpty()) {
            return Option.none();
        }
        return Option.some(max(comparator));
    }

    E max(Comparator<? super E> comparator);

    Traversable<E> reverse();

    default String mkString() {
        return foldLeft(new StringBuilder(), (stringBuilder, e) -> stringBuilder.append(e.toString())).toString();
    }

    default String mkString(String separator) {
        return mkString("", separator, "");
    }

    default String mkString(String start, String separator, String end) {
        return foldLeftWithIndex(new StringBuilder().append(start), (index, builder, e) -> {
            if (index > 0) {
                builder.append(separator);
            }
            return builder.append(e);
        }).append(end).toString();
    }
}

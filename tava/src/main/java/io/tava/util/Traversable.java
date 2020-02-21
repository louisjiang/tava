package io.tava.util;

import io.tava.function.*;
import io.tava.lang.Option;
import io.tava.lang.Tuple2;

public interface Traversable<E> {

    default void println() {
        foreach(System.out::println);
    }

    void foreach(Consumer1<E> action);

    void foreachWithIndex(IndexedConsumer1<E> action);

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

    boolean forall(Predicate1<E> action);

    boolean exists(Predicate1<E> action);

    int count(Predicate1<E> action);

    Option<E> find(Predicate1<E> action);

    default <R> R fold(R zero, Function2<R, E, R> action) {
        return foldLeft(zero, action);
    }

    <R> R foldLeft(R zero, Function2<R, E, R> action);

    <R> R foldLeftWithIndex(R zero, IndexedFunction2<R, E, R> action);

    <R> R foldRight(R zero, Function2<R, E, R> action);

    <R> R foldRightWithIndex(R zero, IndexedFunction2<R, E, R> action);

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

    Traversable<E> reverse();

    default String mkString() {
        return foldLeft(new StringBuilder(), (stringBuilder, e) -> stringBuilder.append(e.toString())).toString();
    }

    default String mkString(String separator) {
        return mkString("", separator, "");
    }

    default String mkString(String start, String separator, String end) {
        StringBuilder stringBuilder = new StringBuilder().append(start);
        foldLeftWithIndex(stringBuilder, (index, stringBuilder1, e) -> {
            if (index > 0) {
                stringBuilder.append(separator);
            }
            return stringBuilder.append(e);
        });
        stringBuilder.append(end);
        return stringBuilder.toString();
    }
}

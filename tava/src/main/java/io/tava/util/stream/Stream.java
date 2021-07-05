package io.tava.util.stream;

import io.reactivex.rxjava3.core.Observable;
import io.tava.function.Consumer1;
import io.tava.function.IndexedConsumer1;
import io.tava.function.Predicate1;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-02-28 16:44
 */
public interface Stream<T> extends java.util.stream.Stream<T>{

    void foreach(Consumer1<T> action);

    void foreachWithIndex(IndexedConsumer1<T> action);

    boolean isEmpty();

    Observable<T> observable();

    Stream<T> filter(Predicate1<T> action);

    Stream<T> filterNot(Predicate1<T> action);

    Stream<T> take(int n);

    Stream<T> takeRight(int n);

    Stream<T> takeWhile(Predicate1<T> action);

    Stream<T> drop(int n);

    Stream<T> dropRight(int n);

    Stream<T> dropWhile(Predicate1<T> action);

    Stream<T> slice(int from, int until);

}

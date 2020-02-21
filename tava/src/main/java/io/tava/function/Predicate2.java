package io.tava.function;

@FunctionalInterface
public interface Predicate2<T1, T2> {

	boolean test(T1 t1, T2 t2);

}
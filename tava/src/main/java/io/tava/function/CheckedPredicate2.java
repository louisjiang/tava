package io.tava.function;

@FunctionalInterface
public interface CheckedPredicate2<T1, T2> {

	boolean test(T1 t1, T2 t2) throws Exception;

}
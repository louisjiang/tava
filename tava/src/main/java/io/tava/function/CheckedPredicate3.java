package io.tava.function;

@FunctionalInterface
public interface CheckedPredicate3<T1, T2, T3> {

	boolean test(T1 t1, T2 t2, T3 t3) throws Throwable;

}
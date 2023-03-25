package io.tava.function;

@FunctionalInterface
public interface CheckedPredicate1<T1> {

	boolean test(T1 t1) throws Exception;

}
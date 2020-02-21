package io.tava.function;

@FunctionalInterface
public interface IndexedPredicate2<T1, T2> {

	boolean test(int index, T1 t1, T2 t2);

}
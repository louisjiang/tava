package io.tava.function;

@FunctionalInterface
public interface IndexedPredicate1<T1> {

	boolean test(int index, T1 t1);

}
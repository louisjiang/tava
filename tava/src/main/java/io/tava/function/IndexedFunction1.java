package io.tava.function;

@FunctionalInterface
public interface IndexedFunction1<T1, R> {

	R apply(int index, T1 t1);

}
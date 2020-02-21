package io.tava.function;

@FunctionalInterface
public interface IndexedFunction2<T1, T2, R> {

	R apply(int index, T1 t1, T2 t2);

}
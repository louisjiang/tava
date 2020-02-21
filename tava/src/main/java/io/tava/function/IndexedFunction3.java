package io.tava.function;

@FunctionalInterface
public interface IndexedFunction3<T1, T2, T3, R> {

	R apply(int index, T1 t1, T2 t2, T3 t3);

}
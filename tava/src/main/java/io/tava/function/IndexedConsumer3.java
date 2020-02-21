package io.tava.function;

@FunctionalInterface
public interface IndexedConsumer3<T1, T2, T3> {

	void accept(int index, T1 t1, T2 t2, T3 t3);

}
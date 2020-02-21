package io.tava.function;

@FunctionalInterface
public interface IndexedConsumer2<T1, T2> {

	void accept(int index, T1 t1, T2 t2);

}
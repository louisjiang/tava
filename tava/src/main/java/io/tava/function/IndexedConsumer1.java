package io.tava.function;

@FunctionalInterface
public interface IndexedConsumer1<T1> {

	void accept(int index, T1 t1);

}
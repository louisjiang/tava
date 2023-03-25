package io.tava.function;

@FunctionalInterface
public interface CheckedConsumer1<T1> {

	void accept(T1 t1) throws Exception;

}
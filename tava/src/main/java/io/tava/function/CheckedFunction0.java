package io.tava.function;

@FunctionalInterface
public interface CheckedFunction0<R> {

    R apply() throws Exception;

}
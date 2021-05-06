package io.tava.pipeline;


import io.tava.function.Function1;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-03-24 17:31
 */
public class Pipeline<I, O> {

    private final Function1<I, O> handler;

    public Pipeline(Function1<I, O> handler) {
        this.handler = handler;
    }

    public <K> Pipeline<I, K> addHandler(Function1<O, K> newHandler) {
        if (newHandler == null) {
            throw new NullPointerException("newHandler is null");
        }
        return new Pipeline<>(input -> newHandler.apply(this.handler.apply(input)));
    }

    public O TO(I input) {
        return this.handler.apply(input);
    }

}

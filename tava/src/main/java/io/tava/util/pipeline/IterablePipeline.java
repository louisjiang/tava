package io.tava.util.pipeline;

import io.tava.function.Function1;
import io.tava.function.Predicate1;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-04 16:15:21
 */
public class IterablePipeline<In> implements Pipeline<In> {

    @Override
    public Pipeline<In> filter(Predicate1<In> action) {
        return null;
    }

    @Override
    public <Out> Pipeline<Out> map(Function1<In, Out> map) {
        return null;
    }
}

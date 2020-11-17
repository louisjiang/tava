package io.tava.queue;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:45:10
 */
public interface WorkHandler<T> extends com.lmax.disruptor.WorkHandler<Event<T>>, Handler<T> {
    @Override
    default void onEvent(Event<T> event) throws Exception {
        onHandle(event);
    }
}
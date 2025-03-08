package io.tava.queue;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:43:33
 */
public interface EventHandler<T> extends com.lmax.disruptor.EventHandler<Event<T>> {

    @Override
    default void onEvent(Event<T> event, long sequence, boolean endOfBatch) throws Exception {
        handle(event.getValue());
        event.setValue(null);
    }

    void handle(T value) throws Exception;

}

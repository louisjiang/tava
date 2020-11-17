package io.tava.queue;


/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:26:27
 */
public class EventFactory<T> implements com.lmax.disruptor.EventFactory<Event<T>> {

    @Override
    public Event<T> newInstance() {
        return new Event<>();
    }

}

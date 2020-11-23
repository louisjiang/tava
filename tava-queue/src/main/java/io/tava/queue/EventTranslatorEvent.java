package io.tava.queue;

import com.lmax.disruptor.EventTranslatorOneArg;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 18:27:35
 */
public class EventTranslatorEvent<T> implements EventTranslatorOneArg<Event<T>, T> {
    @Override
    public void translateTo(Event<T> event, long sequence, T value) {
        event.setValue(value);
    }

}

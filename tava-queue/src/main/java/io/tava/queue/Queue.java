package io.tava.queue;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import io.tava.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:25:57
 */
public class Queue<T> implements ExceptionHandler<Event<T>> {

    private final Logger logger = LoggerFactory.getLogger(Queue.class);
    private final EventTranslatorEvent<T> eventTranslator = new EventTranslatorEvent<>();
    private final Disruptor<Event<T>> disruptor;


    public Queue(int ringBufferSize, EventHandler<T> handler, String threadPrefix) {
        this(ringBufferSize, handler, ProducerType.MULTI, new BlockingWaitStrategy(), threadPrefix, 1);
    }

    public Queue(int ringBufferSize, EventHandler<T> handler, ProducerType producerType, WaitStrategy waitStrategy, String threadPrefix, int threadNumber) {
        if (ringBufferSize < 1) {
            throw new IllegalArgumentException("ringBufferSize must not be less than 1");
        }
        if (Integer.bitCount(ringBufferSize) != 1) {
            throw new IllegalArgumentException("ringBufferSize must be a power of 2");
        }
        this.disruptor = new Disruptor<>(new EventFactory<>(), ringBufferSize, new NamedThreadFactory(threadPrefix), producerType, waitStrategy);
        this.disruptor.setDefaultExceptionHandler(this);
        this.disruptor.handleEventsWith(handlers(handler, threadNumber).toArray(new EventHandler[0]));
        this.disruptor.start();
    }


    public void publish(T value) {
        this.disruptor.publishEvent(this.eventTranslator, value);
    }

    public long remainingCapacity() {
        return disruptor.getRingBuffer().remainingCapacity();
    }


    public void shutdown() {
        this.disruptor.shutdown();
    }

    public void handleEventException(Throwable cause, long sequence, Event<T> event) {
        logger.error("handleEventException", cause);
    }

    private List<EventHandler<T>> handlers(EventHandler<T> handler, int threadNumber) {
        List<EventHandler<T>> handlers = new ArrayList<>();
        for (int i = 0; i < threadNumber; i++) {
            handlers.add(handler);
        }
        return handlers;
    }

    @Override
    public void handleOnStartException(Throwable cause) {
        logger.error("handleOnStartException", cause);
    }

    @Override
    public void handleOnShutdownException(Throwable cause) {
        logger.error("handleOnShutdownException", cause);
    }
}

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

    public Queue(int ringBufferSize, HandlerFactory<T, ? extends Handler<T>> factory, String threadPrefix) {
        this(ringBufferSize, factory, ProducerType.MULTI, new BlockingWaitStrategy(), threadPrefix, Runtime.getRuntime().availableProcessors() * 3);
    }

    public Queue(int ringBufferSize, HandlerFactory<T, ? extends Handler<T>> factory, ProducerType producerType, WaitStrategy waitStrategy, String threadPrefix, int threadNumber) {
        if (ringBufferSize < 1) {
            throw new IllegalArgumentException("ringBufferSize must not be less than 1");
        }
        if (Integer.bitCount(ringBufferSize) != 1) {
            throw new IllegalArgumentException("ringBufferSize must be a power of 2");
        }
        this.disruptor = new Disruptor<>(new EventFactory<>(), ringBufferSize, new NamedThreadFactory(threadPrefix), producerType, waitStrategy);
        this.disruptor.setDefaultExceptionHandler(this);
        if (factory instanceof EventHandlerFactory<?>) {
            EventHandlerFactory<T> eventHandlerFactory = (EventHandlerFactory<T>) factory;
            List<EventHandler<T>> handlers = handlers(eventHandlerFactory, threadNumber);
            this.disruptor.handleEventsWith(handlers.toArray(new EventHandler[0]));
        } else if (factory instanceof WorkHandlerFactory<?>) {
            WorkHandlerFactory<T> workHandlerFactory = (WorkHandlerFactory<T>) factory;
            List<WorkHandler<T>> handlers = handlers(workHandlerFactory, threadNumber);
            this.disruptor.handleEventsWithWorkerPool(handlers.toArray(new WorkHandler[0]));
        } else {
            throw new IllegalArgumentException("factory:" + factory.getClass() + " is error");
        }
        this.disruptor.start();
    }


    public void publish(T value) {
        this.disruptor.publishEvent(this.eventTranslator, value);
    }

    public void shutdown() {
        this.disruptor.shutdown();
    }

    private <H extends Handler<T>> List<H> handlers(HandlerFactory<T, H> factory, int threadNumber) {
        List<H> handlers = new ArrayList<>();
        for (int i = 0; i < threadNumber; i++) {
            handlers.add(factory.newInstance());
        }
        return handlers;
    }

    public void handleEventException(Throwable cause, long sequence, Event<T> event) {
        logger.error("handleEventException", cause);
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

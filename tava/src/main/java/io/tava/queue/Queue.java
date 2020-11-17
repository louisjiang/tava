package io.tava.queue;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.RingBuffer;
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
public class Queue<T> {

    private final Logger logger = LoggerFactory.getLogger(Queue.class);
    private final EventTranslatorEvent<T> eventTranslator = new EventTranslatorEvent<>();
    private final Disruptor<Event<T>> disruptor;
    private final RingBuffer<Event<T>> ringBuffer;


    public Queue(int ringBufferSize,
                 HandlerFactory<T, ? extends Handler<T>> factory, String threadPrefix) {
        this(ringBufferSize, factory, ProducerType.MULTI, new BlockingWaitStrategy(), threadPrefix);
    }

    public Queue(int ringBufferSize,
                 HandlerFactory<T, ? extends Handler<T>> factory,
                 ProducerType producerType,
                 WaitStrategy waitStrategy,
                 String threadPrefix) {
        this.disruptor = new Disruptor<>(new EventFactory<>(), ringBufferSize, new NamedThreadFactory(threadPrefix), producerType, waitStrategy);
        disruptor.setDefaultExceptionHandler(new ExceptionHandler<Event<T>>() {
            @Override
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
        });
        if (factory instanceof EventHandlerFactory) {
            EventHandlerFactory<T> eventHandlerFactory = (EventHandlerFactory<T>) factory;
            List<EventHandler<T>> handlers = handlers(eventHandlerFactory);
            disruptor.handleEventsWith(handlers.toArray(new EventHandler[0]));
        } else if (factory instanceof WorkHandlerFactory) {
            WorkHandlerFactory<T> workHandlerFactory = (WorkHandlerFactory<T>) factory;
            List<WorkHandler<T>> handlers = handlers(workHandlerFactory);
            disruptor.handleEventsWithWorkerPool(handlers.toArray(new WorkHandler[0]));
        }
        this.ringBuffer = disruptor.start();
    }


    public void publish(T value) {
        ringBuffer.publishEvent(eventTranslator, value);
    }

    public void shutdown() {
        this.disruptor.shutdown();
    }

    private <H extends Handler<T>> List<H> handlers(HandlerFactory<T, H> factory) {
        H handler = factory.newInstance();
        List<H> handlers = new ArrayList<>();
        int number = factory.threadNumber();
        for (int i = 0; i < number; i++) {
            handlers.add(handler);
        }
        return handlers;
    }


}

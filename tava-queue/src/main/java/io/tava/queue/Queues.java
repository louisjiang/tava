package io.tava.queue;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.tava.function.Function1;
import net.openhft.hashing.LongHashFunction;

import java.util.ArrayList;
import java.util.List;

public class Queues<E> {

    private static final LongHashFunction longHashFunction = LongHashFunction.murmur_3();
    public final List<Queue<E>> queues = new ArrayList<>();
    private final int queueSize;

    public Queues(int ringBufferSize, int queueSize, HandlerFactory<E, ? extends Handler<E>> factory, String threadPrefix, int threadNumber) {
        this.queueSize = queueSize;
        for (int i = 0; i < queueSize; i++) {
            Queue<E> queue = new Queue<>(ringBufferSize, factory, ProducerType.MULTI, new BlockingWaitStrategy(), threadPrefix + "-" + i, threadNumber);
            this.queues.add(queue);
        }
    }


    public void publish(E value, Function1<E, String> hashKey) {
        long hash = longHashFunction.hashChars(hashKey.apply(value));
        hash = hash ^ (hash >>> 16);
        long index = (this.queueSize - 1) & hash;
        Queue<E> queue = this.queues.get((int) index);
        queue.publish(value);
    }

}

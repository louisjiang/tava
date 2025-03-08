package io.tava.queue;

import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.tava.Tava;
import io.tava.function.Function0;
import io.tava.function.Function1;
import io.tava.lang.Tuple2;
import net.openhft.hashing.LongHashFunction;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class Queues<E> {

    private static final LongHashFunction longHashFunction = LongHashFunction.murmur_3();
    private final AtomicLong counter = new AtomicLong(0);
    private final List<Queue<E>> queues = new ArrayList<>();
    private final int queueSize;

    public Queues(int queueSize, int ringBufferSize, EventHandler<E> handler, ProducerType producerType, Function0<WaitStrategy> waitStrategy, String threadPrefix) {
        this.queueSize = queueSize;
        for (int i = 0; i < queueSize; i++) {
            Queue<E> queue = new Queue<>(ringBufferSize, handler, producerType, waitStrategy.apply(), threadPrefix + "-" + i, 1);
            this.queues.add(queue);
        }
    }

    public Tuple2<Long, Long> publish(E value, Function1<E, String> hashKey) {
        long hash = longHashFunction.hashChars(hashKey.apply(value));
        hash = hash ^ (hash >>> 16);
        long index = (this.queueSize - 1) & hash;
        Queue<E> queue = this.queues.get((int) index);
        queue.publish(value);
        return Tava.of(index, queue.remainingCapacity());
    }


    public Tuple2<Long, Long> publish(E value) {
        long index = this.counter.getAndIncrement();
        if (index == Long.MAX_VALUE) {
            this.counter.set(0);
        }
        index = index % this.queues.size();
        Queue<E> queue = this.queues.get((int) index);
        queue.publish(value);
        return Tava.of(index, queue.remainingCapacity());
    }

}

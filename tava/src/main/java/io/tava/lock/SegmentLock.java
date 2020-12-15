package io.tava.lock;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-12-14 16:13
 */
public class SegmentLock<T> implements Lock<T> {

    private final Map<Integer, ReentrantLock> locks = new ConcurrentHashMap<>();
    private final int segments;

    public SegmentLock() {
        this(Runtime.getRuntime().availableProcessors(), false);
    }

    public SegmentLock(int segments) {
        this(segments, false);
    }

    public SegmentLock(int segments, boolean fair) {
        this.segments = segments;
        for (int i = 0; i < segments; i++) {
            this.locks.put(i, new ReentrantLock(fair));
        }
    }


    public void lock(T key) {
        this.locks.get((key.hashCode() >>> 1) % this.segments).lock();
    }


    public void unlock(T key) {
        this.locks.get((key.hashCode() >>> 1) % this.segments).unlock();
    }


}

package io.tava.lock;


import io.tava.function.CheckedFunction0;
import io.tava.function.Consumer0;
import io.tava.function.Function0;

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


    public void execute(T key, Consumer0 consumer0) {
        try {
            lock(key);
            consumer0.accept();
        } finally {
            unlock(key);
        }
    }

    public <R> void call(T key, Function0<R> function0) {
        try {
            lock(key);
            function0.apply();
        } finally {
            unlock(key);
        }
    }

    public <R> void call(T key, CheckedFunction0<R> function0) throws Throwable {
        try {
            lock(key);
            function0.apply();
        } finally {
            unlock(key);
        }
    }


}

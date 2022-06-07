package io.tava.lock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-12-14 16:10
 */
public class HashLock<T> implements Lock<T> {

    private final SegmentLock<T> segmentLock = new SegmentLock<>();
    private final Map<T, LockInfo> locks = new ConcurrentHashMap<>();
    private final boolean fair;

    public HashLock() {
        this(true);
    }

    public HashLock(boolean fair) {
        this.fair = fair;
    }

    public void lock(T key) {
        this.segmentLock.doWithLock(key, () -> {
            LockInfo lockInfo = this.locks.computeIfAbsent(key, k -> new LockInfo(fair));
            lockInfo.incrementAndGet();
            return lockInfo;
        }).lock();
    }


    public void unlock(T key) {
        LockInfo lockInfo = this.locks.get(key);
        if (lockInfo == null) {
            return;
        }
        if (lockInfo.count() == 1) {
            this.segmentLock.doWithLock(key, () -> {
                if (lockInfo.count() == 1) {
                    this.locks.remove(key);
                }
            });
        }
        lockInfo.decrementAndGet();
        lockInfo.unlock();
    }


    static class LockInfo {

        private final AtomicInteger count = new AtomicInteger(0);
        private final ReentrantLock reentrantLock;

        LockInfo(boolean fair) {
            this.reentrantLock = new ReentrantLock(fair);
        }

        public void lock() {
            this.reentrantLock.lock();
        }

        public void unlock() {
            this.reentrantLock.unlock();
        }

        public void incrementAndGet() {
            this.count.incrementAndGet();
        }

        public void decrementAndGet() {
            this.count.decrementAndGet();
        }

        public int count() {
            return this.count.get();
        }
    }
}

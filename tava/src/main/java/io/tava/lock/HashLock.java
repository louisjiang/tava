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
        this(false);
    }

    public HashLock(boolean fair) {
        this.fair = fair;
    }

    public void lock(T key) {
        this.segmentLock.lock(key);
        LockInfo lockInfo = this.locks.get(key);
        if (lockInfo == null) {
            lockInfo = new LockInfo(fair);
            this.locks.put(key, lockInfo);
        } else {
            lockInfo.incrementAndGet();
        }
        this.segmentLock.unlock(key);
        lockInfo.lock();
    }


    public void unlock(T key) {
        LockInfo lockInfo = locks.get(key);
        if (lockInfo.count() == 1) {
            segmentLock.lock(key);
            if (lockInfo.count.get() == 1) {
                locks.remove(key);
            }
            segmentLock.unlock(key);
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

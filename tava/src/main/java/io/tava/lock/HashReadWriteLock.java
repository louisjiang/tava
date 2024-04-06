package io.tava.lock;

import io.tava.lang.Option;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-06-23 11:53
 */
public class HashReadWriteLock<T> implements ReadWriteLock<T> {
    private final SegmentLock<T> segmentLock = new SegmentLock<>();
    private final Map<T, LockInfo> lockInfos = new ConcurrentHashMap<>();
    private final boolean fair;
    public HashReadWriteLock() {
        this(false);
    }

    public HashReadWriteLock(boolean fair) {
        this.fair = fair;
    }

    @Override
    public boolean isWriteLocked(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        LockInfo lockInfo = lockInfos.get(key);
        if (lockInfo == null) {
            return false;
        }
        return lockInfo.isWriteLocked();
    }

    public void writeLock(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        this.segmentLock.doWithLock(key, () -> {
            LockInfo lockInfo = this.lockInfos.computeIfAbsent(key, k -> new LockInfo(fair));
            lockInfo.incrementWrite();
            return lockInfo;
        }).writeLock();
    }

    @Override
    public boolean isReadLocked(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        LockInfo lockInfo = lockInfos.get(key);
        if (lockInfo == null) {
            return false;
        }
        return lockInfo.isReadLocked();
    }

    public void readLock(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        this.segmentLock.doWithLock(key, () -> {
            LockInfo lockInfo = this.lockInfos.computeIfAbsent(key, k -> new LockInfo(fair));
            lockInfo.incrementRead();
            return lockInfo;
        }).readLock();
    }

    public void unWriteLock(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        this.getLockInfo(key).forEach(lockInfo -> {
            lockInfo.decrementWrite();
            lockInfo.unWriteLock();
        });
    }

    public void unReadLock(T key) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        this.getLockInfo(key).forEach(lockInfo -> {
            lockInfo.decrementRead();
            lockInfo.unReadLock();
        });
    }

    private Option<LockInfo> getLockInfo(T key) {
        LockInfo lockInfo = this.lockInfos.get(key);
        if (lockInfo == null) {
            return Option.none();
        }
        if (lockInfo.count() == 1) {
            this.segmentLock.doWithLock(key, () -> {
                if (lockInfo.count() == 1) {
                    this.lockInfos.remove(key);
                }
            });
        }
        return Option.some(lockInfo);
    }


    private static class LockInfo {

        private final AtomicInteger readCount = new AtomicInteger(0);
        private final AtomicInteger writeCount = new AtomicInteger(0);
        private final ReentrantReadWriteLock readWriteLock;

        LockInfo(boolean fair) {
            this.readWriteLock = new ReentrantReadWriteLock(fair);
        }


        public boolean isWriteLocked() {
            return this.readWriteLock.isWriteLocked();
        }

        public boolean isReadLocked() {
            return this.readWriteLock.getReadHoldCount() != 0;
        }

        public void writeLock() {
            this.readWriteLock.writeLock().lock();
        }

        public void unWriteLock() {
            this.readWriteLock.writeLock().unlock();
        }

        public void readLock() {
            this.readWriteLock.readLock().lock();
        }

        public void unReadLock() {
            this.readWriteLock.readLock().unlock();
        }

        public void incrementWrite() {
            this.writeCount.incrementAndGet();
        }

        public void decrementWrite() {
            this.writeCount.decrementAndGet();
        }

        public void incrementRead() {
            this.readCount.incrementAndGet();
        }

        public void decrementRead() {
            this.readCount.decrementAndGet();
        }

        public int readCount() {
            return this.readCount.get();
        }

        public int writeCount() {
            return this.writeCount.get();
        }

        public int count() {
            return readCount() + writeCount();
        }

    }
}

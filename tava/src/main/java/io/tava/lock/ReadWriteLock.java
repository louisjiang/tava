package io.tava.lock;

import io.tava.function.Consumer0;
import io.tava.function.Function0;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-06-23 15:18
 */
public interface ReadWriteLock<T> {

    void writeLock(T key);

    void readLock(T key);

    void unWriteLock(T key);

    void unReadLock(T key);

    default void doWithWriteLock(T key, Consumer0 consumer) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            this.writeLock(key);
            consumer.accept();
        } finally {
            this.unWriteLock(key);
        }
    }

    default <R> R doWithWriteLock(T key, Function0<R> function) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            this.writeLock(key);
            return function.apply();
        } finally {
            this.unWriteLock(key);
        }
    }


    default void doWithReadLock(T key, Consumer0 consumer) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            this.readLock(key);
            consumer.accept();
        } finally {
            this.unReadLock(key);
        }
    }

    default <R> R doWithReadLock(T key, Function0<R> function) {
        if (key == null) {
            throw new NullPointerException("key is null");
        }
        try {
            this.readLock(key);
            return function.apply();
        } finally {
            this.unReadLock(key);
        }
    }

}

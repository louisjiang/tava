package io.tava.lock;

import io.tava.function.Consumer0;
import io.tava.function.Function0;
import io.tava.util.Util;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-12-14 19:47
 */
public interface Lock<T> extends Util {

    void lock(T key);

    void unlock(T key);


    default void doWithLock(T key, Consumer0 consumer) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        try {
            lock(key);
            consumer.accept();
        } finally {
            unlock(key);
        }
    }

    default <R> R doWithLock(T key, Function0<R> function) {
        if (isEmpty(key)) {
            throw new NullPointerException("key is null");
        }
        try {
            lock(key);
            return function.apply();
        } finally {
            unlock(key);
        }

    }


}

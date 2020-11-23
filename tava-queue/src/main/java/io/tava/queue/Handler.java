package io.tava.queue;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:41:37
 */
public interface Handler<T> {

    default void onHandle(Event<T> event) throws Exception {
        handle(event.getValue());
    }

    void handle(T value) throws Exception;

}

package io.tava.queue;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:26:40
 */
public class Event<T> {

    private T value;

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}

package io.tava.queue;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:47:52
 */
public interface HandlerFactory<T, H extends Handler<T>> {

    H newInstance();

}

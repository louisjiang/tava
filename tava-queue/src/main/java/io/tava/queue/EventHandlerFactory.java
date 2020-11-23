package io.tava.queue;

/**
 * 消费组中的每个消费者都会对消息m进行消费，各个消费者之间不存在竞争。
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:48:55
 * @see io.tava.queue.WorkHandlerFactory
 */
public interface EventHandlerFactory<T> extends HandlerFactory<T, EventHandler<T>> {

}

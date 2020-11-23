package io.tava.queue;

/**
 * 消费组的消费者对于同一条消息不重复消费；也就是说，如果消费者1消费了消息m，消费者2不在消费消息m。
 *
 * @author louisjiang <493509534@qq.com>
 * @version 2020-03-31 10:49:42
 * @see io.tava.queue.EventHandlerFactory
 */
public interface WorkHandlerFactory<T> extends HandlerFactory<T, WorkHandler<T>> {

}

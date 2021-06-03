package io.tava.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;
import io.tava.Tava;
import io.tava.db.Database;
import io.tava.lang.Tuple2;
import io.tava.queue.Queue;
import io.tava.queue.WorkHandler;
import io.tava.queue.WorkHandlerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 14:09
 */
public class DatabaseCache extends MemoryCache implements WorkHandlerFactory<Tuple2<String, Object>>, WorkHandler<Tuple2<String, Object>> {

    private final Database database;
    private final Queue<Tuple2<String, Object>> queue;

    public DatabaseCache(com.github.benmanes.caffeine.cache.LoadingCache<String, Object> cache,
                         Database database,
                         int ringBufferSize) {
        super(cache);
        this.database = database;
        this.queue = new Queue<>(ringBufferSize, this, ProducerType.MULTI, new BlockingWaitStrategy(), "queue-database-" + database.path());
    }

    @Override
    public void put(String key, Object value) {
        super.put(key, value);
        this.queue.publish(Tava.of(key, value));
    }

    @Override
    public WorkHandler<Tuple2<String, Object>> newInstance() {
        return this;
    }

    @Override
    public void handle(Tuple2<String, Object> value) throws Exception {
        this.database.put(value.getValue1().getBytes(StandardCharsets.UTF_8), JSON.toJSONBytes(value.getValue2(), SerializerFeature.WriteClassName));
    }

}

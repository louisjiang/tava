package io.tava.cache;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.tava.db.Database;
import io.tava.lang.Tuple2;
import io.tava.queue.Handler;
import io.tava.queue.HandlerFactory;

import java.nio.charset.StandardCharsets;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-31 14:09
 */
public class DatabaseCache extends MemoryCache implements HandlerFactory<Tuple2<String, Object>, Handler<Tuple2<String, Object>>>, Handler<Tuple2<String, Object>> {

    private final Database database;

    public DatabaseCache(com.github.benmanes.caffeine.cache.LoadingCache<String, Object> cache, Database database) {
        super(cache);
        this.database = database;
    }

    @Override
    public void put(String key, Object value) {
        super.put(key, value);
    }

    @Override
    public Handler<Tuple2<String, Object>> newInstance() {
        return this;
    }

    @Override
    public void handle(Tuple2<String, Object> value) throws Exception {
        this.database.put(value.getValue1().getBytes(StandardCharsets.UTF_8), JSON.toJSONBytes(value.getValue2(), SerializerFeature.WriteClassName));
    }
}

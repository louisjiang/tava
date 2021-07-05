package io.tava.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferOutput;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import io.tava.function.Consumer1;
import io.tava.function.Function1;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.nio.ByteBuffer;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-03 13:41
 */
public class KryoPool extends BasePooledObjectFactory<Kryo> {

    private final GenericObjectPool<Kryo> pool;
    private final Consumer1<Kryo> configurator;

    public KryoPool(Consumer1<Kryo> configurator,
                    int maxTotal,
                    int maxIdle,
                    int minIdle,
                    long maxWaitMillis) {
        this.configurator = configurator;
        this.pool = new GenericObjectPool<>(this, createPoolConfig(maxTotal, maxIdle, minIdle, maxWaitMillis));
    }


    public Kryo borrowKryo() throws Exception {
        return this.pool.borrowObject();
    }

    public void returnKryo(Kryo kryo) {
        if (kryo == null) {
            return;
        }
        this.pool.returnObject(kryo);
    }

    public ByteBuffer toByteBuffer(Object value) throws Exception {
        return execute(kryo -> {
            UnsafeByteBufferOutput output = new UnsafeByteBufferOutput(1024 * 1024, Integer.MAX_VALUE);
            kryo.writeClassAndObject(output, value);
            return output.getByteBuffer();
        });
    }

    public <R> R execute(Function1<Kryo, R> function) throws Exception {
        Kryo kryo = borrowKryo();
        try {
            return function.apply(kryo);
        } finally {
            returnKryo(kryo);
        }
    }

    @Override
    public Kryo create() throws Exception {
        Kryo kryo = new Kryo(new MapReferenceResolver());
        kryo.setAutoReset(true);
        kryo.setCopyReferences(true);
        kryo.setReferences(true);
        this.configurator.accept(kryo);
        return kryo;
    }

    @Override
    public PooledObject<Kryo> wrap(Kryo kryo) {
        return new DefaultPooledObject<>(kryo);
    }

    private GenericObjectPoolConfig<Kryo> createPoolConfig(int maxTotal,
                                                           int maxIdle,
                                                           int minIdle,
                                                           long maxWaitMillis) {
        GenericObjectPoolConfig<Kryo> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        return poolConfig;
    }
}

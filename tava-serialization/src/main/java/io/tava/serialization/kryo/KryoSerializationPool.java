package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import io.tava.function.Consumer1;
import io.tava.function.Function1;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-03 13:41
 */
public class KryoSerializationPool extends BasePooledObjectFactory<Kryo> {

    private final GenericObjectPool<Kryo> pool;
    private final Consumer1<Kryo> configurator;

    public KryoSerializationPool() {
        this(null);
    }

    public KryoSerializationPool(Consumer1<Kryo> configurator) {
        this(configurator, 64, 64, 32, 100);
    }

    public KryoSerializationPool(Consumer1<Kryo> configurator, int maxTotal, int maxIdle, int minIdle, long maxWaitMilliseconds) {
        this.configurator = configurator;
        this.pool = new GenericObjectPool<>(this, createPoolConfig(maxTotal, maxIdle, minIdle, maxWaitMilliseconds));
    }

    public Kryo borrowObject() throws Exception {
        return this.pool.borrowObject();
    }

    public void returnObject(Kryo value) {
        if (value == null) {
            return;
        }
        this.pool.returnObject(value);
    }

    public byte[] toBytes(Object value) throws Exception {
        return toOutput(new UnsafeOutput(1024, Integer.MAX_VALUE), value).toBytes();
    }

    public Object toObject(byte[] bytes) throws Exception {
        return fromInput(new Input(bytes));
    }

    public byte[] toBytes(Kryo kryo, Object value) throws Exception {
        UnsafeOutput output = new UnsafeOutput(1024, Integer.MAX_VALUE);
        kryo.writeClassAndObject(output, value);
        return output.toBytes();
    }

    public Object toObject(Kryo kryo, byte[] bytes) throws Exception {
        return kryo.readClassAndObject(new Input(bytes));
    }

    public <O extends Output> O toOutput(O output, Object value) throws Exception {
        return execute(kryo -> {
            kryo.writeClassAndObject(output, value);
            return output;
        });
    }

    public Object fromInput(Input input) throws Exception {
        return execute(kryo -> kryo.readClassAndObject(input));
    }


    public <R> R execute(Function1<Kryo, R> function) throws Exception {
        Kryo kryo = borrowObject();
        try {
            return function.apply(kryo);
        } finally {
            returnObject(kryo);
        }
    }

    @Override
    public Kryo create() throws Exception {
        KryoSerialization kryo = new KryoSerialization(new SubclassResolver(), new MapReferenceResolver());
        if (this.configurator != null) {
            this.configurator.accept(kryo);
        }
        return kryo;
    }

    @Override
    public PooledObject<Kryo> wrap(Kryo kryo) {
        return new DefaultPooledObject<>(kryo);
    }

    private GenericObjectPoolConfig<Kryo> createPoolConfig(int maxTotal, int maxIdle, int minIdle, long maxWaitMilliseconds) {
        GenericObjectPoolConfig<Kryo> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        if (maxWaitMilliseconds > 0) {
            poolConfig.setMaxWait(java.time.Duration.ofMillis(maxWaitMilliseconds));
        }
        return poolConfig;
    }
}

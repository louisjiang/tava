package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferInput;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferOutput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import io.tava.function.Consumer1;
import io.tava.function.Function1;
import io.tava.serialization.Serialization;
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
public class KryoSerializationPool extends BasePooledObjectFactory<Kryo> implements Serialization {

    private final GenericObjectPool<Kryo> pool;
    private final Consumer1<Kryo> configurator;

    public KryoSerializationPool() {
        this(null);
    }

    public KryoSerializationPool(Consumer1<Kryo> configurator) {
        this(configurator, 64, 64, 32, 100);
    }

    public KryoSerializationPool(Consumer1<Kryo> configurator,
                                 int maxTotal,
                                 int maxIdle,
                                 int minIdle,
                                 long maxWaitMilliseconds) {
        this.configurator = configurator;
        this.pool = new GenericObjectPool<>(this, createPoolConfig(maxTotal, maxIdle, minIdle, maxWaitMilliseconds));
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
        return toOutput(new UnsafeByteBufferOutput(1024, Integer.MAX_VALUE), value).getByteBuffer();
    }

    public byte[] toBytes(Object value) throws Exception {
        return toOutput(new UnsafeOutput(1024, Integer.MAX_VALUE), value).toBytes();
    }

    public <O extends Output> O toOutput(O output, Object value) throws Exception {
        return execute(kryo -> {
            kryo.writeClassAndObject(output, value);
            return output;
        });
    }

    public Object fromByteBuffer(ByteBuffer byteBuffer) throws Exception {
        return fromInput(new UnsafeByteBufferInput(byteBuffer));
    }

    public Object toObject(byte[] bytes) throws Exception {
        return fromInput(new Input(bytes));
    }

    public Object fromInput(Input input) throws Exception {
        return execute(kryo -> kryo.readClassAndObject(input));
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

    private GenericObjectPoolConfig<Kryo> createPoolConfig(int maxTotal,
                                                           int maxIdle,
                                                           int minIdle,
                                                           long maxWaitMilliseconds) {
        GenericObjectPoolConfig<Kryo> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(java.time.Duration.ofMillis(maxWaitMilliseconds));
        return poolConfig;
    }
}

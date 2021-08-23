package io.tava.serialization;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.ClosureSerializer;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferOutput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import io.tava.function.Consumer1;
import io.tava.function.Function1;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import java.lang.invoke.SerializedLambda;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-03 13:41
 */
public class KryoSerialization extends BasePooledObjectFactory<Kryo> {

    private final GenericObjectPool<Kryo> pool;
    private final Consumer1<Kryo> configurator;

    public KryoSerialization() {
        this(null);
    }

    public KryoSerialization(Consumer1<Kryo> configurator) {
        this(configurator, 64, 64, 32, 100);
    }

    public KryoSerialization(Consumer1<Kryo> configurator,
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

    public Object fromBytes(byte[] bytes) throws Exception {
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
        Kryo kryo = new Kryo(new MapReferenceResolver());
        kryo.setAutoReset(true);
        kryo.setCopyReferences(true);
        kryo.setReferences(true);

        kryo.register(Class.class, 11);
        kryo.register(LinkedHashMap.class, 12);
        kryo.register(HashMap.class, 13);
        kryo.register(TreeMap.class, 14);
        kryo.register(Hashtable.class, 15);
        kryo.register(ConcurrentHashMap.class, 16);
        kryo.register(ArrayList.class, 17);
        kryo.register(LinkedList.class, 18);
        kryo.register(LinkedHashSet.class, 19);
        kryo.register(HashSet.class, 20);
        kryo.register(Optional.class, 21);
        kryo.register(BigDecimal.class, 22);
        kryo.register(byte.class, 23);
        kryo.register(char.class, 24);
        kryo.register(short.class, 25);
        kryo.register(int.class, 26);
        kryo.register(float.class, 27);
        kryo.register(double.class, 28);
        kryo.register(long.class, 29);
        kryo.register(boolean.class, 30);
        kryo.register(String.class, 31);
        kryo.register(byte[].class, 32);
        kryo.register(char[].class, 33);
        kryo.register(short[].class, 34);
        kryo.register(int[].class, 35);
        kryo.register(float[].class, 36);
        kryo.register(double[].class, 37);
        kryo.register(long[].class, 38);
        kryo.register(boolean[].class, 39);
        kryo.register(String[].class, 40);
        kryo.register(Object[].class, 41);
        kryo.register(Date.class, 42);

        kryo.register(ClosureSerializer.Closure.class, new ClosureSerializer(), 43);
        kryo.register(SerializedLambda.class, 44);

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
                                                           long maxWaitMillis) {
        GenericObjectPoolConfig<Kryo> poolConfig = new GenericObjectPoolConfig<>();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxWait(Duration.ofMillis(maxWaitMillis));
        return poolConfig;
    }
}

package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.ByteBufferInput;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.ClosureSerializer;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferInput;
import com.esotericsoftware.kryo.unsafe.UnsafeByteBufferOutput;
import com.esotericsoftware.kryo.unsafe.UnsafeOutput;
import com.esotericsoftware.kryo.util.MapReferenceResolver;
import io.tava.function.Consumer1;
import io.tava.function.Function1;
import io.tava.serialization.Serialization;
import io.tava.serialization.kryo.serializer.joda.*;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import sun.nio.ch.DirectBuffer;

import java.lang.invoke.SerializedLambda;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-03 13:41
 */
public class KryoSerialization extends BasePooledObjectFactory<Kryo> implements Serialization {

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
        if (byteBuffer instanceof DirectBuffer) {
            return fromInput(new UnsafeByteBufferInput(byteBuffer));
        }
        return fromInput(new ByteBufferInput(byteBuffer));
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
        kryo.setRegistrationRequired(true);
        kryo.setAutoReset(true);

        kryo.register(Class.class, 11);
        kryo.register(HashMap.class, 12);
        kryo.register(LinkedHashMap.class, 13);
        kryo.register(TreeMap.class, 14);
        kryo.register(Hashtable.class, 15);
        kryo.register(ConcurrentHashMap.class, 16);
        kryo.register(ArrayList.class, 17);
        kryo.register(LinkedList.class, 18);
        kryo.register(HashSet.class, 19);
        kryo.register(LinkedHashSet.class, 20);
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
        kryo.register(Byte.class, 31);
        kryo.register(Character.class, 32);
        kryo.register(Short.class, 33);
        kryo.register(Integer.class, 34);
        kryo.register(Float.class, 35);
        kryo.register(Double.class, 36);
        kryo.register(Long.class, 37);
        kryo.register(Boolean.class, 38);
        kryo.register(String.class, 39);
        kryo.register(byte[].class, 40);
        kryo.register(char[].class, 41);
        kryo.register(short[].class, 42);
        kryo.register(int[].class, 43);
        kryo.register(float[].class, 44);
        kryo.register(double[].class, 45);
        kryo.register(long[].class, 46);
        kryo.register(boolean[].class, 47);
        kryo.register(Byte[].class, 48);
        kryo.register(Character[].class, 49);
        kryo.register(Short[].class, 50);
        kryo.register(Integer[].class, 51);
        kryo.register(Float[].class, 52);
        kryo.register(Double[].class, 53);
        kryo.register(Long[].class, 54);
        kryo.register(Boolean[].class, 55);
        kryo.register(String[].class, 56);
        kryo.register(Object[].class, 57);
        kryo.register(Date.class, 58);

        kryo.register(org.joda.time.DateTime.class, DateTimeSerializer.getInstance(), 59);
        kryo.register(org.joda.time.Duration.class, DurationSerializer.getInstance(), 60);
        kryo.register(org.joda.time.Instant.class, InstantSerializer.getInstance(), 61);
        kryo.register(org.joda.time.Interval.class, IntervalSerializer.getInstance(), 62);
        kryo.register(org.joda.time.LocalDate.class, LocalDateSerializer.getInstance(), 63);
        kryo.register(org.joda.time.LocalTime.class, LocalTimeSerializer.getInstance(), 64);
        kryo.register(org.joda.time.LocalDateTime.class, LocalDateTimeSerializer.getInstance(), 65);
        kryo.register(org.joda.time.YearMonth.class, YearMonthSerializer.getInstance(), 66);
        kryo.register(org.joda.time.MonthDay.class, MonthDaySerializer.getInstance(), 67);
        kryo.register(org.joda.time.Period.class, PeriodSerializer.getInstance(), 68);
        kryo.register(org.joda.time.Years.class, BaseSingleFieldPeriodSerializer.getInstance(), 69);
        kryo.register(org.joda.time.Months.class, BaseSingleFieldPeriodSerializer.getInstance(), 70);
        kryo.register(org.joda.time.Days.class, BaseSingleFieldPeriodSerializer.getInstance(), 71);
        kryo.register(org.joda.time.Weeks.class, BaseSingleFieldPeriodSerializer.getInstance(), 72);
        kryo.register(org.joda.time.Hours.class, BaseSingleFieldPeriodSerializer.getInstance(), 73);
        kryo.register(org.joda.time.Minutes.class, BaseSingleFieldPeriodSerializer.getInstance(), 74);
        kryo.register(org.joda.time.Seconds.class, BaseSingleFieldPeriodSerializer.getInstance(), 75);

        kryo.register(java.time.Duration.class, 76);
        kryo.register(java.time.Instant.class, 77);
        kryo.register(java.time.LocalDate.class, 78);
        kryo.register(java.time.LocalTime.class, 79);
        kryo.register(java.time.LocalDateTime.class, 80);
        kryo.register(java.time.ZoneOffset.class, 81);
        kryo.register(java.time.ZoneId.class, 82);
        kryo.register(java.time.OffsetTime.class, 83);
        kryo.register(java.time.OffsetDateTime.class, 84);
        kryo.register(java.time.ZonedDateTime.class, 85);
        kryo.register(java.time.Year.class, 86);
        kryo.register(java.time.YearMonth.class, 87);
        kryo.register(java.time.MonthDay.class, 88);
        kryo.register(java.time.Period.class, 89);

        kryo.register(ClosureSerializer.Closure.class, new ClosureSerializer(), 90);
        kryo.register(SerializedLambda.class, 91);

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

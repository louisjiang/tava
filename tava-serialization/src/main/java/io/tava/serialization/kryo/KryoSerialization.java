package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.*;
import com.esotericsoftware.kryo.serializers.ClosureSerializer;
import io.tava.serialization.kryo.serializer.joda.*;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.lang.invoke.SerializedLambda;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-05-17 16:16
 */
public class KryoSerialization extends Kryo {

    public KryoSerialization(ReferenceResolver referenceResolver) {
        this(new SubclassResolver(), referenceResolver);
    }

    public KryoSerialization(ClassResolver classResolver, ReferenceResolver referenceResolver) {
        super(classResolver, referenceResolver);
        setInstantiatorStrategy(new StdInstantiatorStrategy());
        setAutoReset(true);
        setCopyReferences(true);
        setReferences(true);
        setRegistrationRequired(true);
        setAutoReset(true);

        register(Class.class, 11);
        register(LinkedHashMap.class, 12);
        register(HashMap.class, 13);
        register(TreeMap.class, 14);
        register(Hashtable.class, 15);
        register(ConcurrentHashMap.class, 16);
        register(Map.class, 17);
        register(ArrayList.class, 18);
        register(LinkedList.class, 19);
        register(List.class, 20);
        register(LinkedHashSet.class, 21);
        register(HashSet.class, 22);
        register(Set.class, 23);
        register(Optional.class, 24);
        register(BigDecimal.class, 25);
        register(BigInteger.class, 26);
        register(byte[].class, 27);
        register(char[].class, 28);
        register(short[].class, 29);
        register(int[].class, 30);
        register(float[].class, 31);
        register(double[].class, 32);
        register(long[].class, 33);
        register(boolean[].class, 34);
        register(Byte[].class, 35);
        register(Character[].class, 36);
        register(Short[].class, 37);
        register(Integer[].class, 38);
        register(Float[].class, 39);
        register(Double[].class, 40);
        register(Long[].class, 41);
        register(Boolean[].class, 42);
        register(String[].class, 43);
        register(Object[].class, 44);
        register(Date.class, 45);

        register(org.joda.time.DateTime.class, DateTimeSerializer.getInstance(), 46);
        register(org.joda.time.Duration.class, DurationSerializer.getInstance(), 47);
        register(org.joda.time.Instant.class, InstantSerializer.getInstance(), 48);
        register(org.joda.time.Interval.class, IntervalSerializer.getInstance(), 49);
        register(org.joda.time.LocalDate.class, LocalDateSerializer.getInstance(), 50);
        register(org.joda.time.LocalTime.class, LocalTimeSerializer.getInstance(), 51);
        register(org.joda.time.LocalDateTime.class, LocalDateTimeSerializer.getInstance(), 52);
        register(org.joda.time.YearMonth.class, YearMonthSerializer.getInstance(), 53);
        register(org.joda.time.MonthDay.class, MonthDaySerializer.getInstance(), 54);
        register(org.joda.time.Period.class, PeriodSerializer.getInstance(), 55);
        register(org.joda.time.Years.class, BaseSingleFieldPeriodSerializer.getInstance(), 56);
        register(org.joda.time.Months.class, BaseSingleFieldPeriodSerializer.getInstance(), 57);
        register(org.joda.time.Days.class, BaseSingleFieldPeriodSerializer.getInstance(), 58);
        register(org.joda.time.Weeks.class, BaseSingleFieldPeriodSerializer.getInstance(), 59);
        register(org.joda.time.Hours.class, BaseSingleFieldPeriodSerializer.getInstance(), 60);
        register(org.joda.time.Minutes.class, BaseSingleFieldPeriodSerializer.getInstance(), 61);
        register(org.joda.time.Seconds.class, BaseSingleFieldPeriodSerializer.getInstance(), 62);

        register(java.time.Duration.class, 63);
        register(java.time.Instant.class, 64);
        register(java.time.LocalDate.class, 65);
        register(java.time.LocalTime.class, 66);
        register(java.time.LocalDateTime.class, 67);
        register(java.time.ZoneOffset.class, 68);
        register(java.time.ZoneId.class, 69);
        register(java.time.OffsetTime.class, 70);
        register(java.time.OffsetDateTime.class, 71);
        register(java.time.ZonedDateTime.class, 72);
        register(java.time.Year.class, 73);
        register(java.time.YearMonth.class, 74);
        register(java.time.MonthDay.class, 75);
        register(java.time.Period.class, 76);

        register(ClosureSerializer.Closure.class, new ClosureSerializer(), 77);
        register(SerializedLambda.class, 78);
    }

    @Override
    public Registration register(Class type, int id) {
        Registration registration = getClassResolver().getRegistration(type);
        if (registration == null) {
            registration = getClassResolver().getRegistration(id);
        }
        if (registration == null) {
            return register(type, getDefaultSerializer(type), id);
        }
        if (registration.getType() == type && registration.getId() != id) {
            throw new KryoException("An existing [" + registration.getType() + "] already use id [" + registration.getId() + "],new id [" + id + "]");
        }
        if (registration.getType() != type && registration.getId() == id) {
            throw new KryoException("An existing [" + registration.getType() + "] already use id [" + registration.getId() + "], [" + type + "] register the same id again");
        }
        return register(type, getDefaultSerializer(type), id);
    }
}

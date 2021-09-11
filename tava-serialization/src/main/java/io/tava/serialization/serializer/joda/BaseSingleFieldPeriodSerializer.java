package io.tava.serialization.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.*;
import org.joda.time.base.BaseSingleFieldPeriod;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 14:35
 */
public class BaseSingleFieldPeriodSerializer extends Serializer<BaseSingleFieldPeriod> {


    private static final BaseSingleFieldPeriodSerializer INSTANCE = new BaseSingleFieldPeriodSerializer();

    public static BaseSingleFieldPeriodSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, BaseSingleFieldPeriod value) {
        output.writeInt(unbox(value), true);
    }

    @Override
    public BaseSingleFieldPeriod read(Kryo kryo, Input input, Class<? extends BaseSingleFieldPeriod> type) {
        return box(type, input.readInt(true));
    }

    private BaseSingleFieldPeriod box(Class<? extends BaseSingleFieldPeriod> type, int value) {
        if (Years.class.isAssignableFrom(type)) {
            return Years.years(value);
        }
        if (Months.class.isAssignableFrom(type)) {
            return Months.months(value);
        }
        if (Days.class.isAssignableFrom(type)) {
            return Days.days(value);
        }
        if (Weeks.class.isAssignableFrom(type)) {
            return Weeks.weeks(value);
        }
        if (Hours.class.isAssignableFrom(type)) {
            return Hours.hours(value);
        }
        if (Minutes.class.isAssignableFrom(type)) {
            return Minutes.minutes(value);
        }
        if (Seconds.class.isAssignableFrom(type)) {
            return Seconds.seconds(value);
        }
        return null;
    }

    private int unbox(BaseSingleFieldPeriod value) {
        if (value instanceof Years) {
            return ((Years) value).getYears();
        }
        if (value instanceof Months) {
            return ((Months) value).getMonths();
        }
        if (value instanceof Days) {
            return ((Days) value).getDays();
        }
        if (value instanceof Weeks) {
            return ((Weeks) value).getWeeks();
        }
        if (value instanceof Hours) {
            return ((Hours) value).getHours();
        }
        if (value instanceof Minutes) {
            return ((Minutes) value).getMinutes();
        }
        if (value instanceof Seconds) {
            return ((Seconds) value).getSeconds();
        }
        return 0;
    }


}

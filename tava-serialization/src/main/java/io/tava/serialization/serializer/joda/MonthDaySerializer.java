package io.tava.serialization.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Chronology;
import org.joda.time.MonthDay;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 14:25
 */
public class MonthDaySerializer extends Serializer<MonthDay> {

    private static final MonthDaySerializer INSTANCE = new MonthDaySerializer();

    public static MonthDaySerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, MonthDay monthDay) {
        output.writeInt(monthDay.getMonthOfYear());
        output.writeInt(monthDay.getDayOfMonth());
        output.writeString(IdentifiableChronology.idOfValue(monthDay.getChronology()));
    }

    @Override
    public MonthDay read(Kryo kryo, Input input, Class<? extends MonthDay> type) {
        int monthOfYear = input.readInt(true);
        int dayOfMonth = input.readInt(true);
        Chronology chronology = IdentifiableChronology.valueOfId(input.readString());
        return new MonthDay(monthOfYear, dayOfMonth, chronology);
    }
}

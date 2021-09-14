package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Chronology;
import org.joda.time.LocalDateTime;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 13:42
 */
public class LocalDateTimeSerializer extends Serializer<LocalDateTime> {

    private static final LocalDateTimeSerializer INSTANCE = new LocalDateTimeSerializer();

    public static LocalDateTimeSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, LocalDateTime localDateTime) {
        output.writeInt(localDateTime.getYear(), true);
        output.writeInt(localDateTime.getMonthOfYear(), true);
        output.writeInt(localDateTime.getDayOfMonth(), true);
        output.writeInt(localDateTime.getHourOfDay(), true);
        output.writeInt(localDateTime.getMinuteOfHour(), true);
        output.writeInt(localDateTime.getSecondOfMinute(), true);
        output.writeInt(localDateTime.getMillisOfSecond(), true);
        output.writeString(IdentifiableChronology.idOfValue(localDateTime.getChronology()));
    }

    @Override
    public LocalDateTime read(Kryo kryo, Input input, Class<? extends LocalDateTime> type) {
        int year = input.readInt(true);
        int monthOfYear = input.readInt(true);
        int dayOfMonth = input.readInt(true);
        int hourOfDay = input.readInt(true);
        int minuteOfHour = input.readInt(true);
        int secondOfMinute = input.readInt(true);
        int millisOfSecond = input.readInt(true);
        Chronology chronology = IdentifiableChronology.valueOfId(input.readString());
        return new LocalDateTime(year, monthOfYear, dayOfMonth, hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, chronology);
    }
}

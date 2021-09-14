package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Chronology;
import org.joda.time.LocalTime;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 14:11
 */
public class LocalTimeSerializer extends Serializer<LocalTime> {

    private static final LocalTimeSerializer INSTANCE = new LocalTimeSerializer();

    public static LocalTimeSerializer getInstance() {
        return INSTANCE;
    }


    @Override
    public void write(Kryo kryo, Output output, LocalTime localTime) {
        output.writeInt(localTime.getHourOfDay(), true);
        output.writeInt(localTime.getMinuteOfHour(), true);
        output.writeInt(localTime.getSecondOfMinute(), true);
        output.writeInt(localTime.getMillisOfSecond(), true);
        output.writeString(IdentifiableChronology.idOfValue(localTime.getChronology()));
    }

    @Override
    public LocalTime read(Kryo kryo, Input input, Class<? extends LocalTime> type) {
        int hourOfDay = input.readInt(true);
        int minuteOfHour = input.readInt(true);
        int secondOfMinute = input.readInt(true);
        int millisOfSecond = input.readInt(true);
        Chronology chronology = IdentifiableChronology.valueOfId(input.readString());
        return new LocalTime(hourOfDay, minuteOfHour, secondOfMinute, millisOfSecond, chronology);
    }

}

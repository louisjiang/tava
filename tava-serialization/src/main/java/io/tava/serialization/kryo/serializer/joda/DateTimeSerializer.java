package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.DateTime;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 12:27
 */
public class DateTimeSerializer extends Serializer<DateTime> {

    private static final DateTimeSerializer INSTANCE = new DateTimeSerializer();

    public static DateTimeSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, DateTime dateTime) {
        output.writeLong(dateTime.getMillis(), true);
        output.writeString(IdentifiableChronology.idOfValue(dateTime.getChronology()));
    }

    @Override
    public DateTime read(Kryo kryo, Input input, Class<? extends DateTime> type) {
        return new DateTime(input.readLong(true), IdentifiableChronology.valueOfId(input.readString()));
    }

}

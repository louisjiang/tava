package io.tava.serialization.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.LocalDate;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 13:40
 */
public class LocalDateSerializer extends Serializer<LocalDate> {

    private static final LocalDateSerializer INSTANCE = new LocalDateSerializer();

    public static LocalDateSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, LocalDate localDate) {
        output.writeInt(localDate.getYear(), true);
        output.writeInt(localDate.getMonthOfYear(), true);
        output.writeInt(localDate.getDayOfMonth(), true);
        output.writeString(IdentifiableChronology.idOfValue(localDate.getChronology()));
    }

    @Override
    public LocalDate read(Kryo kryo, Input input, Class<? extends LocalDate> type) {
        return new LocalDate(input.readInt(true), input.readInt(true), input.readInt(true));
    }

}

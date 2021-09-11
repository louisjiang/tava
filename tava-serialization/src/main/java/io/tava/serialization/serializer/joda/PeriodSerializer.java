package io.tava.serialization.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Period;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-11 14:29
 */
public class PeriodSerializer extends Serializer<Period> {

    private static final PeriodSerializer INSTANCE = new PeriodSerializer();

    public static PeriodSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, Period period) {
        output.writeString(period.toString());
    }

    @Override
    public Period read(Kryo kryo, Input input, Class<? extends Period> type) {
        return Period.parse(input.readString());
    }
}

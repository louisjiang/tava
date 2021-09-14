package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Interval;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 13:36
 */
public class IntervalSerializer extends Serializer<Interval> {

    private static final IntervalSerializer INSTANCE = new IntervalSerializer();

    public static IntervalSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, Interval interval) {
        output.writeString(interval.toString());
    }

    @Override
    public Interval read(Kryo kryo, Input input, Class<? extends Interval> type) {
        return Interval.parse(input.readString());
    }
}

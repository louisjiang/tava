package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Duration;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 12:33
 */
public class DurationSerializer extends Serializer<Duration> {

    private static final DurationSerializer INSTANCE = new DurationSerializer();

    public static DurationSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, Duration duration) {
        output.writeLong(duration.getMillis(), true);
    }

    @Override
    public Duration read(Kryo kryo, Input input, Class<? extends Duration> type) {
        return new Duration(input.readLong(true));
    }
}

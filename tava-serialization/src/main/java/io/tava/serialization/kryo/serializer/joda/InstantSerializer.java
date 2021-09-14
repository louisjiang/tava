package io.tava.serialization.kryo.serializer.joda;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.joda.time.Instant;


/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-10 12:46
 */
public class InstantSerializer extends Serializer<Instant> {

    private static final InstantSerializer INSTANCE = new InstantSerializer();

    public static InstantSerializer getInstance() {
        return INSTANCE;
    }

    @Override
    public void write(Kryo kryo, Output output, Instant instant) {
        output.writeLong(instant.getMillis(), true);
    }

    @Override
    public Instant read(Kryo kryo, Input input, Class<? extends Instant> type) {
        return new Instant(input.readLong(true));
    }

}

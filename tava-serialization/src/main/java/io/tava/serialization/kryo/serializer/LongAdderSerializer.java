package io.tava.serialization.kryo.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Serializer;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import java.util.concurrent.atomic.LongAdder;

public class LongAdderSerializer extends Serializer<LongAdder> {
    @Override
    public void write(Kryo kryo, Output output, LongAdder object) {
        output.writeLong(object.longValue());
    }

    @Override
    public LongAdder read(Kryo kryo, Input input, Class<? extends LongAdder> type) {
        LongAdder longAdder = new LongAdder();
        longAdder.add(input.readLong());
        return longAdder;
    }
}

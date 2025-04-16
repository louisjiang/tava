package io.tava.serialization.kryo;

import com.esotericsoftware.kryo.Kryo;

public interface Serialization {

    Kryo create() throws Exception;

    Object toObject(byte[] bytes) throws Exception;

    byte[] toBytes(Object value) throws Exception;

}

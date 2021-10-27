package io.tava.serialization;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-09-13 13:09
 */
public interface Serialization {

    byte[] toBytes(Object value) throws Exception;

    Object toObject(byte[] bytes) throws Exception;

}

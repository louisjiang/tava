package io.tava.db.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-05 09:33
 */
public class Serialization {

    public static byte[] toBytes(ByteBuffer byteBuffer) {
        if (byteBuffer.hasArray()) {
            return byteBuffer.array();
        }
        int position = byteBuffer.position();
        byte[] dest = new byte[position];
        byteBuffer.get(dest, 0, position);
        return dest;
    }

    public static byte[] toBytes(String value) {
        if (value == null) {
            return null;
        }
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] toBytes(Object value) {
        if (value == null) {
            return null;
        }
        return JSON.toJSONBytes(value, SerializerFeature.WriteClassName);
    }

    public static String toString(byte[] value) {
        if (value == null) {
            return null;
        }
        return new String(value, StandardCharsets.UTF_8);
    }

    public static <T> T toObject(byte[] bytes) {
        return (T)JSON.parse(bytes, Feature.SupportAutoType);
    }


}

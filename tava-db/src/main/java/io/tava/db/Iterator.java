package io.tava.db;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-06 14:19
 */
public interface Iterator extends java.util.Iterator<Iterator.Entry>, Closeable {

    class Entry {

        private final byte[] key;
        private final byte[] value;
        private final AbstractDatabase database;

        Entry(byte[] key, byte[] value, AbstractDatabase database) {
            this.key = key;
            this.value = value;
            this.database = database;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }

        public String getStringKey() {
            return new String(key, StandardCharsets.UTF_8);
        }

        @SuppressWarnings("unchecked")
        public <T> T getObjectValue() {
            if (value == null || value.length == 0) {
                return null;
            }
            return (T) this.database.toObject(value);
        }

    }

}

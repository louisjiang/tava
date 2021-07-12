package io.tava.db;

import io.tava.db.util.Serialization;

import java.io.Closeable;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-07-06 14:19
 */
public interface Iterator extends Closeable {

    boolean hasNext();

    Entry next();

    class Entry {

        private final byte[] key;
        private final byte[] value;

        Entry(byte[] key, byte[] value) {
            this.key = key;
            this.value = value;
        }

        public byte[] getKey() {
            return key;
        }

        public byte[] getValue() {
            return value;
        }

        public String getStringKey() {
            return Serialization.toString(key);
        }

        public Object getObjectValue() {
            return Serialization.toObject(value);
        }

    }

}

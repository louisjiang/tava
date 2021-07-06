package io.tava.db;

import io.tava.Tava;
import io.tava.db.util.Serialization;
import io.tava.lang.Tuple2;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.lmdbjava.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Set;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-05-17 15:03
 */
public class LmdbDatabase implements Database {

    private static final Logger LOGGER = LoggerFactory.getLogger(LmdbDatabase.class);
    private static Method cleanerMethod;
    private static Method cleanMethod;
    private final Env<ByteBuffer> env;
    private final Dbi<ByteBuffer> dbi;
    private final File path;
    private final GenericObjectPool<ByteBuffer> keyPool;

    static {
        try {
            cleanerMethod = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");
            cleanMethod.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException ignored) {
        }

    }


    public LmdbDatabase(String path, long size, int maxReaders, int maxDbs) {
        this.path = new File(path);
        this.path.mkdirs();
        this.env = Env.create().setMapSize(size * 1024 * 1024).setMaxReaders(maxReaders).setMaxDbs(maxDbs).open(this.path, EnvFlags.MDB_NOLOCK);
        this.dbi = this.env.openDbi("default", DbiFlags.MDB_CREATE);
        GenericObjectPoolConfig<ByteBuffer> genericObjectPoolConfig = new GenericObjectPoolConfig<>();
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        genericObjectPoolConfig.setMaxTotal(availableProcessors * 3);
        genericObjectPoolConfig.setMaxIdle(availableProcessors * 3);
        genericObjectPoolConfig.setMinIdle(availableProcessors);
        this.keyPool = new GenericObjectPool<>(new BasePooledObjectFactory<ByteBuffer>() {
            @Override
            public ByteBuffer create() throws Exception {
                return ByteBuffer.allocateDirect(env.getMaxKeySize());
            }

            @Override
            public void passivateObject(PooledObject<ByteBuffer> p) throws Exception {
                p.getObject().clear();
            }

            @Override
            public PooledObject<ByteBuffer> wrap(ByteBuffer byteBuffer) {
                return new DefaultPooledObject<>(byteBuffer);
            }
        }, genericObjectPoolConfig);
    }

    @Override
    public void put(String key, Object value) {
        ByteBuffer keyByteBuffer = allocateKeyByteBuffer(key);
        if (keyByteBuffer == null) {
            return;
        }
        ByteBuffer valueByteBuffer = allocateValueByteBuffer(value);
        this.dbi.put(keyByteBuffer, valueByteBuffer);
        returnKeyByteBuffer(keyByteBuffer);
        releaseDirect(valueByteBuffer);
    }

    @Override
    public void put(Map<String, Object> keyValues) {
        Set<Map.Entry<String, Object>> entries = keyValues.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            this.put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void delete(String key) {
        ByteBuffer keyByteBuffer = allocateKeyByteBuffer(key);
        if (keyByteBuffer == null) {
            return;
        }
        this.dbi.delete(keyByteBuffer);
        returnKeyByteBuffer(keyByteBuffer);
    }

    @Override
    public void delete(Set<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    public ByteBuffer get(ByteBuffer key) {
        Txn<ByteBuffer> txn = this.env.txnWrite();
        ByteBuffer valueByteBuffer = this.dbi.get(txn, key);
        txn.commit();
        return valueByteBuffer;
    }

    @Override
    public byte[] get(String key) {
        ByteBuffer keyByteBuffer = allocateKeyByteBuffer(key);
        if (keyByteBuffer == null) {
            return null;
        }
        ByteBuffer valueByteBuffer = get(keyByteBuffer);
        if (valueByteBuffer == null) {
            returnKeyByteBuffer(keyByteBuffer);
            return null;
        }
        byte[] value = new byte[valueByteBuffer.position()];
        valueByteBuffer.get(value);
        returnKeyByteBuffer(keyByteBuffer);
        releaseDirect(valueByteBuffer);
        return value;
    }

    @Override
    public Iterator iterator() {
        Txn<ByteBuffer> txn = this.env.txnRead();
        final CursorIterable<ByteBuffer> iterate = dbi.iterate(txn);
        final java.util.Iterator<CursorIterable.KeyVal<ByteBuffer>> iterator = iterate.iterator();
        return new Iterator() {
            @Override
            public void close() throws IOException {
                iterate.close();
                txn.commit();
                txn.close();
            }

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public Tuple2<String, Object> next() {
                CursorIterable.KeyVal<ByteBuffer> next = iterator.next();
                String key = Serialization.toString(Serialization.toBytes(next.key()));
                Object value = Serialization.toObject(Serialization.toBytes(next.val()));
                return Tava.of(key, value);
            }
        };
    }

    @Override
    public void commit() {

    }

    @Override
    public String path() {
        return path.getAbsolutePath();
    }

    @Override
    public void close() {
        this.keyPool.close();
        this.dbi.close();
        this.env.close();
    }

    private ByteBuffer allocateKeyByteBuffer(String key) {
        try {
            ByteBuffer byteBuffer = this.keyPool.borrowObject();
            byteBuffer.put(Serialization.toBytes(key)).flip();
            return byteBuffer;
        } catch (Exception cause) {
            LOGGER.error("allocateKeyByteBuffer:[{}]", path, cause);
            return null;
        }

    }

    private ByteBuffer allocateValueByteBuffer(Object value) {
        byte[] bytes = Serialization.toBytes(value);
        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(bytes.length);
        valueBuffer.put(bytes).flip();
        return valueBuffer;
    }

    private void returnKeyByteBuffer(ByteBuffer byteBuffer) {
        if (byteBuffer == null) {
            return;
        }
        this.keyPool.returnObject(byteBuffer);
    }

    private void releaseDirect(ByteBuffer byteBuffer) {
        if (byteBuffer == null || !byteBuffer.isDirect()) {
            return;
        }
        try {
            Object cleaner = cleanerMethod.invoke(byteBuffer);
            cleanMethod.invoke(cleaner);
        } catch (IllegalAccessException | InvocationTargetException cause) {
            LOGGER.error("cleanDirect:[{}]", path, cause);
        }
    }
}

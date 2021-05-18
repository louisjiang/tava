package io.tava.db.lmdb;

import io.tava.db.AbstractWriteBatch;
import io.tava.db.Database;
import io.tava.db.WriteBatch;
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
import java.nio.file.Files;
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
    private final File file;
    private final GenericObjectPool<ByteBuffer> keyPool;


    static {
        try {
            cleanerMethod = Class.forName("java.nio.DirectByteBuffer").getMethod("cleaner");
            cleanerMethod.setAccessible(true);
            cleanMethod = Class.forName("sun.misc.Cleaner").getMethod("clean");
            cleanMethod.setAccessible(true);
        } catch (NoSuchMethodException | ClassNotFoundException cause) {
        }

    }


    public LmdbDatabase(File file, long mapSize, int maxReaders) {
        this.file = file;
        if (!file.exists()) {
            try {
                Files.createDirectories(file.toPath());
            } catch (IOException cause) {
                LOGGER.error("createDirectories:[{}]", file, cause);
            }
        }
        this.env = Env.create().setMapSize(mapSize).setMaxReaders(maxReaders).open(file, EnvFlags.MDB_NOLOCK);
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
    public void put(byte[] key, byte[] value) {
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
    public void delete(byte[] key) {
        ByteBuffer keyByteBuffer = allocateKeyByteBuffer(key);
        if (keyByteBuffer == null) {
            return;
        }
        this.dbi.delete(keyByteBuffer);
        returnKeyByteBuffer(keyByteBuffer);
    }

    @Override
    public byte[] get(byte[] key) {
        ByteBuffer keyByteBuffer = allocateKeyByteBuffer(key);
        if (keyByteBuffer == null) {
            return null;
        }
        Txn<ByteBuffer> txn = this.env.txnWrite();
        ByteBuffer valueByteBuffer = this.dbi.get(txn, keyByteBuffer);
        txn.commit();
        if (valueByteBuffer == null) {
            returnKeyByteBuffer(keyByteBuffer);
            return null;
        }
        byte[] value = new byte[valueByteBuffer.remaining()];
        valueByteBuffer.get(value);
        returnKeyByteBuffer(keyByteBuffer);
        releaseDirect(valueByteBuffer);
        return value;
    }

    @Override
    public WriteBatch writeBatch() {
        return new AbstractWriteBatch() {
            @Override
            public void commit() {
                for (byte[] delete : this.deletes) {
                    delete(delete);
                }
                Set<Map.Entry<byte[], byte[]>> entries = this.puts.entrySet();
                for (Map.Entry<byte[], byte[]> entry : entries) {
                    put(entry.getKey(), entry.getValue());
                }
            }
        };
    }

    @Override
    public void close() {
        this.env.close();
    }

    private ByteBuffer allocateKeyByteBuffer(byte[] key) {
        try {
            ByteBuffer byteBuffer = this.keyPool.borrowObject();
            byteBuffer.put(key).flip();
            return byteBuffer;
        } catch (Exception cause) {
            LOGGER.error("allocateKeyByteBuffer:[{}]", file, cause);
            return null;
        }

    }

    private ByteBuffer allocateValueByteBuffer(byte[] b) {
        ByteBuffer valueBuffer = ByteBuffer.allocateDirect(b.length);
        valueBuffer.put(b).flip();
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
            LOGGER.error("cleanDirect:[{}]", file, cause);
        }
    }
}

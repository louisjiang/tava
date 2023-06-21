package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer0;
import io.tava.function.Function0;

import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-04-18 16:01
 */
public abstract class AbstractSegment implements Segment {

    protected final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    protected final Database database;
    protected final String tableName;
    protected final String key;
    protected Map<String, Object> status;

    protected AbstractSegment(Database database, String tableName, String key) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
    }

    @Override
    public void commit() {
        this.database.commit(this.tableName);
    }


    @Override
    public void setStatus(Map<String, Object> status) {
        this.status = status;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> getStatus() {
        Map<String, Object> map = this.database.get(this.tableName, this.key);
        if (map == null) {
            return null;
        }
        return (Map<String, Object>) map.get("status");
    }

    protected <R> R readLock(Function0<R> function0) {
        try {
            this.readWriteLock.readLock().lock();
            return function0.apply();
        } finally {
            this.readWriteLock.readLock().unlock();
        }
    }

    protected <R> R writeLock(Function0<R> function0) {
        try {
            this.readWriteLock.writeLock().lock();
            return function0.apply();
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }

    protected void writeLock(Consumer0 consumer0) {
        try {
            this.readWriteLock.writeLock().lock();
            consumer0.accept();
        } finally {
            this.readWriteLock.writeLock().unlock();
        }
    }
}

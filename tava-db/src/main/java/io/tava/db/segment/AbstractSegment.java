package io.tava.db.segment;

import io.tava.db.Database;
import io.tava.function.Consumer0;
import io.tava.function.Function0;
import io.tava.lock.HashReadWriteLock;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-04-18 16:01
 */
public abstract class AbstractSegment implements Segment {

    protected final HashReadWriteLock<String> readWriteLock = new HashReadWriteLock<>();
    protected final Database database;
    protected final String tableName;
    protected final String key;
    protected Object status;

    protected AbstractSegment(Database database, String tableName, String key) {
        this.database = database;
        this.tableName = tableName;
        this.key = key;
    }

    @Override
    public void commit() {
        this.database.commit(this.tableName + "@status");
        this.database.commit(this.tableName);
    }


    @Override
    public void updateStatus(Object status) {
        this.status = status;
        this.updateStatus();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getStatus() {
//        Map<String, Object> map = this.database.get(this.tableName + "@status", this.key);
//        if (map == null) {
//            return null;
//        }
//        return (V) map.get("status");
        return (V) status;
    }

    protected <R> R readLock(String segmentKey, Function0<R> function0) {
        return this.readWriteLock.doWithReadLock(segmentKey, function0);
    }


    protected void readLock(String segmentKey, Consumer0 consumer0) {
        this.readWriteLock.doWithReadLock(segmentKey, consumer0);
    }

    protected <R> R writeLock(String segmentKey, Function0<R> function0) {
        return this.readWriteLock.doWithWriteLock(segmentKey, function0);
    }

    protected void writeLock(String segmentKey, Consumer0 consumer0) {
        this.readWriteLock.doWithWriteLock(segmentKey, consumer0);
    }

    abstract void updateStatus();
}

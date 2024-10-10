package io.tava.db.segment;

import io.tava.db.Database;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2022-04-18 16:01
 */
public abstract class AbstractSegment implements Segment {

    protected final Database database;
    protected final String tableName;
    protected final String key;
    protected Object statusData;

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
    public void updateStatusData(Object statusData) {
        this.statusData = statusData;
        this.updateStatus();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> V getStatusData() {
        return (V) statusData;
    }

    abstract void updateStatus();
}

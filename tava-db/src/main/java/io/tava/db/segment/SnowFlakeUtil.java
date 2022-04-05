package io.tava.db.segment;

import io.tava.util.SnowFlake;

public class SnowFlakeUtil {

    private static final SnowFlake snowFlake = new SnowFlake(1, 1);

    private SnowFlakeUtil() {
    }

    public static long nextId() {
        return snowFlake.nextId();
    }

}

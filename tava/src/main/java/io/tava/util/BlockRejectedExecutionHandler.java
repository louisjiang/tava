package io.tava.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-06-03 12:38
 */
public class BlockRejectedExecutionHandler implements RejectedExecutionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlockRejectedExecutionHandler.class);
    private static final BlockRejectedExecutionHandler handler = new BlockRejectedExecutionHandler();

    private BlockRejectedExecutionHandler() {

    }

    @Override
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        try {
            LOGGER.warn("CorePoolSize:[{}],PoolSize:[{}],QueueSize:[{}]", executor.getCorePoolSize(), executor.getPoolSize(), executor.getQueue().size());
            executor.getQueue().put(r);
        } catch (InterruptedException cause) {
            LOGGER.error("Work discarded, thread was interrupted while waiting for space to schedule: {}", r);
        }
    }

    public static RejectedExecutionHandler getInstance() {
        return handler;
    }

}

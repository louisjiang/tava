package io.tava.util;

import one.util.streamex.StreamEx;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2021-02-26 17:21
 */
public class Threads implements Util {

    public static final Threads threads = new Threads();

    public static Threads getInstance() {
        return threads;
    }

    private Threads() {
    }

    public String detect() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if (!threadBean.isThreadCpuTimeSupported()) {
            throw new RuntimeException("thread CPU time is not supported on this JDK");
        }


        long currentThreadId = Thread.currentThread().getId();
        long[] threadIds = threadBean.getAllThreadIds();
        List<ThreadInfo> threadInfoList = new ArrayList<>();

        for (long threadId : threadIds) {
            if (currentThreadId == threadId) {
                continue;
            }
            ThreadInfo info = threadBean.getThreadInfo(threadId, Integer.MAX_VALUE);
            if (info == null) {
                continue;
            }
            threadInfoList.add(info);
        }
        StringBuilder builder = new StringBuilder();
        builder.append("PEAK THREAD:").append(threadBean.getPeakThreadCount());
        builder.append(",DAEMON THREAD:").append(threadBean.getDaemonThreadCount());
        builder.append(",TOTAL STARTED THREAD:").append(threadBean.getTotalStartedThreadCount());
        builder.append(",THREAD:").append(threadBean.getThreadCount());
        ;

        builder.append(",BLOCKED:").append(StreamEx.of(threadInfoList).count(threadInfo -> threadInfo.getThreadState().equals(Thread.State.BLOCKED)));
        builder.append(",RUNNABLE:").append(StreamEx.of(threadInfoList).count(threadInfo -> threadInfo.getThreadState().equals(Thread.State.RUNNABLE)));
        builder.append(",WAITING:").append(StreamEx.of(threadInfoList).count(threadInfo -> threadInfo.getThreadState().equals(Thread.State.WAITING)));
        builder.append(",TIMED_WAITING:").append(StreamEx.of(threadInfoList).count(threadInfo -> threadInfo.getThreadState().equals(Thread.State.TIMED_WAITING)));
        builder.append("\r\n\r\n");
        for (ThreadInfo info : threadInfoList) {
            builder.append("\"").append(info.getThreadName()).append("\"");
            builder.append(" id=").append(info.getThreadId());
            Thread.State threadState = info.getThreadState();
            builder.append(" ").append(threadState);
            String lockName = info.getLockName();
            if (nonEmpty(lockName)) {
                builder.append(" on ").append(lockName);
            }
            String lockOwnerName = info.getLockOwnerName();
            if (nonEmpty(lockOwnerName)) {
                builder.append(" owned by ").append(lockOwnerName).append(" id=").append(info.getLockOwnerId());
            }
            if (info.isSuspended()) {
                builder.append(" (suspended)");
            }
            if (info.isInNative()) {
                builder.append(" (in native)");
            }
            builder.append("\r\n");
            StackTraceElement[] stackTraces = info.getStackTrace();
            int length = stackTraces.length;
            int index = 0;
            while (index < length) {
                if (index == 0) {
                    builder.append("\tjava.lang.Thread.State: ").append(threadState).append("\r\n");
                }
                StackTraceElement stackTrace = stackTraces[index];
                builder.append("\t\t").append(stackTrace.toString()).append("\r\n");
                if (index == 0 && info.getLockInfo() != null) {
                    builder.append("\t\t- ").append(threadState).append(" on ").append(info.getLockInfo()).append("\r\n");
                }
                for (MonitorInfo lockedMonitor : info.getLockedMonitors()) {
                    if (lockedMonitor.getLockedStackDepth() == index) {
                        builder.append("\t\t- LOCKED MONITOR on").append(lockedMonitor).append(",locked stack frame:").append(lockedMonitor.getLockedStackFrame().toString()).append("\r\n");
                    }
                }
                index++;
            }
            LockInfo[] lockInfos = info.getLockedSynchronizers();
            if (nonEmpty(lockInfos)) {
                builder.append("\r\n\tNumber of locked synchronizers = ").append(lockInfos.length);
                for (LockInfo lockInfo : lockInfos) {
                    builder.append("\t- ").append(lockInfo).append("\r\n");
                }
            }
            builder.append("\r\n");
        }

        return builder.toString();
    }

}

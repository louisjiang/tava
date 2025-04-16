package io.tava.util;

import org.joda.time.DateTime;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.*;

/**
 * @author louisjiang <493509534@qq.com>
 * @version 2020-05-27 15:35
 */
public class HotThreads {

    private long interval = 1000;
    private int busiestThreads = 10;
    private int threadElementsSnapshotDelay = 10;
    private int threadElementsSnapshotCount = 10;
    private String type = "cpu";
    private boolean ignoreIdleThreads = true;

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setBusiestThreads(int busiestThreads) {
        this.busiestThreads = busiestThreads;
    }

    public void setIgnoreIdleThreads(boolean ignoreIdleThreads) {
        this.ignoreIdleThreads = ignoreIdleThreads;
    }

    public void setThreadElementsSnapshotDelay(int threadElementsSnapshotDelay) {
        this.threadElementsSnapshotDelay = threadElementsSnapshotDelay;
    }

    public void setThreadElementsSnapshotCount(int threadElementsSnapshotCount) {
        this.threadElementsSnapshotCount = threadElementsSnapshotCount;
    }

    public void setType(String type) {
        if ("cpu".equals(type) || "wait".equals(type) || "block".equals(type)) {
            this.type = type;
        } else {
            throw new IllegalArgumentException("type not supported [" + type + "]");
        }
    }

    public String detect() throws Exception {
        return innerDetect();
    }

    private static boolean isIdleThread(ThreadInfo threadInfo) {
        String threadName = threadInfo.getThreadName();

        // NOTE: these are likely JVM dependent
        if (threadName.equals("Signal Dispatcher") ||
                threadName.equals("Finalizer") ||
                threadName.equals("Reference Handler") ||
                threadName.equals("DestroyJavaVM") ||
                threadName.equals("Attach Listener")) {
            return true;
        }

        for (StackTraceElement frame : threadInfo.getStackTrace()) {
            String className = frame.getClassName();
            String methodName = frame.getMethodName();
            if (className.equals("java.util.concurrent.ThreadPoolExecutor") &&
                    methodName.equals("getTask")) {
                return true;
            }
            if (className.equals("sun.nio.ch.SelectorImpl") &&
                    methodName.equals("select")) {
                return true;
            }
            if (className.equals("java.util.concurrent.LinkedTransferQueue") &&
                    methodName.equals("poll")) {
                return true;
            }
            if (className.equals("sun.misc.Unsafe") && methodName.equals("park")) {
                return true;
            }
            if(className.equals("com.lmax.disruptor.BlockingWaitStrategy") && methodName.equals("waitFor")) {
                return true;
            }
            if(className.equals("jdk.internal.misc.Unsafe") && methodName.equals("park")) {
                return true;
            }
        }

        return false;
    }

    private String innerDetect() throws Exception {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        if (!threadBean.isThreadCpuTimeSupported()) {
            throw new RuntimeException("thread CPU time is not supported on this JDK");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Hot threads at ");
        builder.append(new DateTime().toString("yyyy-MM-dd HH:mm:ss.SSS"));
        builder.append(", type=");
        builder.append(type);
        builder.append(", interval=");
        builder.append(interval);
        builder.append(", busiestThreads=");
        builder.append(busiestThreads);
        builder.append(", ignoreIdleThreads=");
        builder.append(ignoreIdleThreads);
        builder.append(", threadElementsSnapshotDelay=");
        builder.append(threadElementsSnapshotDelay);
        builder.append(", threadElementsSnapshotCount=");
        builder.append(threadElementsSnapshotCount);
        builder.append("\n");

        Map<Long, MyThreadInfo> threadInfos = new HashMap<>();
        for (long threadId : threadBean.getAllThreadIds()) {
            // ignore our own thread...
            if (Thread.currentThread().getId() == threadId) {
                continue;
            }
            long cpu = threadBean.getThreadCpuTime(threadId);
            if (cpu == -1) {
                continue;
            }
            ThreadInfo info = threadBean.getThreadInfo(threadId, 0);
            if (info == null) {
                continue;
            }
            threadInfos.put(threadId, new MyThreadInfo(cpu, info));
        }
        Thread.sleep(interval);
        for (long threadId : threadBean.getAllThreadIds()) {
            // ignore our own thread...
            if (Thread.currentThread().getId() == threadId) {
                continue;
            }
            long cpu = threadBean.getThreadCpuTime(threadId);
            if (cpu == -1) {
                threadInfos.remove(threadId);
                continue;
            }
            ThreadInfo info = threadBean.getThreadInfo(threadId, 0);
            if (info == null) {
                threadInfos.remove(threadId);
                continue;
            }
            MyThreadInfo data = threadInfos.get(threadId);
            if (data != null) {
                data.setDelta(cpu, info);
            } else {
                threadInfos.remove(threadId);
            }
        }
        // sort by delta CPU time on thread.
        List<MyThreadInfo> hotties = new ArrayList<>(threadInfos.values());
        final int busiestThreads = Math.min(this.busiestThreads, hotties.size());
        // skip that for now
        hotties.sort((o1, o2) -> {
            if ("cpu".equals(type)) {
                return (int) (o2.cpuTime - o1.cpuTime);
            } else if ("wait".equals(type)) {
                return (int) (o2.waitedTime - o1.waitedTime);
            } else if ("block".equals(type)) {
                return (int) (o2.blockedTime - o1.blockedTime);
            }
            throw new IllegalArgumentException("expected thread type to be either 'cpu', 'wait', or 'block', but was " + type);
        });
        // analyse N stack traces for M busiest threads
        long[] ids = new long[busiestThreads];
        for (int i = 0; i < busiestThreads; i++) {
            MyThreadInfo info = hotties.get(i);
            ids[i] = info.info.getThreadId();
        }
        ThreadInfo[][] allInfos = new ThreadInfo[threadElementsSnapshotCount][];
        for (int j = 0; j < threadElementsSnapshotCount; j++) {
            // NOTE, javadoc of getThreadInfo says: If a thread of the given ID is not alive or does not exist,
            // null will be set in the corresponding element in the returned array. A thread is alive if it has
            // been started and has not yet died.
            allInfos[j] = threadBean.getThreadInfo(ids, Integer.MAX_VALUE);
            Thread.sleep(threadElementsSnapshotDelay);
        }
        for (int t = 0; t < busiestThreads; t++) {
            long time = 0;
            if ("cpu".equals(type)) {
                time = hotties.get(t).cpuTime;
            } else if ("wait".equals(type)) {
                time = hotties.get(t).waitedTime;
            } else if ("block".equals(type)) {
                time = hotties.get(t).blockedTime;
            }
            String threadName = null;
            for (ThreadInfo[] info : allInfos) {
                if (info != null && info[t] != null) {
                    if (ignoreIdleThreads && isIdleThread(info[t])) {
                        info[t] = null;
                        continue;
                    }
                    threadName = info[t].getThreadName();
                    break;
                }
            }
            if (threadName == null) {
                continue; // thread is not alive yet or died before the first snapshot - ignore it!
            }
            double percent = (((double) time) / (interval * 1000 * 1000)) * 100;
            builder.append(String.format(Locale.ROOT, "%n%4.1f%% (%s out of %s) %s usage by thread '%s'%n", percent, time / 1000 / 1000, interval, type, threadName));
            // for each snapshot (2nd array index) find later snapshot for same thread with max number of
            // identical StackTraceElements (starting from end of each)
            boolean[] done = new boolean[threadElementsSnapshotCount];
            for (int i = 0; i < threadElementsSnapshotCount; i++) {
                if (done[i]) continue;
                int maxSim = 1;
                boolean[] similars = new boolean[threadElementsSnapshotCount];
                for (int j = i + 1; j < threadElementsSnapshotCount; j++) {
                    if (done[j]) continue;
                    int similarity = similarity(allInfos[i][t], allInfos[j][t]);
                    if (similarity > maxSim) {
                        maxSim = similarity;
                        similars = new boolean[threadElementsSnapshotCount];
                    }
                    if (similarity == maxSim) similars[j] = true;
                }
                // print out trace maxSim levels of i, and mark similar ones as done
                int count = 1;
                for (int j = i + 1; j < threadElementsSnapshotCount; j++) {
                    if (similars[j]) {
                        done[j] = true;
                        count++;
                    }
                }
                if (allInfos[i][t] != null) {
                    final StackTraceElement[] show = allInfos[i][t].getStackTrace();
                    if (count == 1) {
                        if (show.length > 0) {
                            builder.append(String.format(Locale.ROOT, "  unique snapshot%n"));
                            for (StackTraceElement stackTraceElement : show) {
                                builder.append(String.format(Locale.ROOT, "    %s%n", stackTraceElement));
                            }
                        }
                    } else {
                        builder.append(String.format(Locale.ROOT, "  %d/%d snapshots sharing following %d elements%n", count, threadElementsSnapshotCount, maxSim));
                        for (int l = show.length - maxSim; l < show.length; l++) {
                            builder.append(String.format(Locale.ROOT, "    %s%n", show[l]));
                        }
                    }
                }
            }
        }
        return builder.toString();
    }

    private static final StackTraceElement[] EMPTY = new StackTraceElement[0];

    private int similarity(ThreadInfo threadInfo, ThreadInfo threadInfo0) {
        StackTraceElement[] s1 = threadInfo == null ? EMPTY : threadInfo.getStackTrace();
        StackTraceElement[] s2 = threadInfo0 == null ? EMPTY : threadInfo0.getStackTrace();
        int i = s1.length - 1;
        int j = s2.length - 1;
        int rslt = 0;
        while (i >= 0 && j >= 0 && s1[i].equals(s2[j])) {
            rslt++;
            i--;
            j--;
        }
        return rslt;
    }


    static class MyThreadInfo {
        long cpuTime;
        long blockedCount;
        long blockedTime;
        long waitedCount;
        long waitedTime;
        boolean deltaDone;
        ThreadInfo info;

        MyThreadInfo(long cpuTime, ThreadInfo info) {
            blockedCount = info.getBlockedCount();
            blockedTime = info.getBlockedTime();
            waitedCount = info.getWaitedCount();
            waitedTime = info.getWaitedTime();
            this.cpuTime = cpuTime;
            this.info = info;
        }

        void setDelta(long cpuTime, ThreadInfo info) {
            if (deltaDone) throw new IllegalStateException("setDelta already called once");
            blockedCount = info.getBlockedCount() - blockedCount;
            blockedTime = info.getBlockedTime() - blockedTime;
            waitedCount = info.getWaitedCount() - waitedCount;
            waitedTime = info.getWaitedTime() - waitedTime;
            this.cpuTime = cpuTime - this.cpuTime;
            deltaDone = true;
            this.info = info;
        }
    }
}

package cn.howardliu.monitor.cynomys.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * <br>created at 17-8-15
 *
 * @author liuxh
 * @since 0.0.1
 */
public final class ThreadMXBeanUtils {
    private static final ThreadMXBean THREAD_BEAN = ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_ENABLED = THREAD_BEAN.isThreadCpuTimeSupported()
            && THREAD_BEAN.isThreadCpuTimeEnabled();

    private ThreadMXBeanUtils() {
    }

    public static long getCurrentThreadCpuTime() {
        return getThreadCpuTime(Thread.currentThread().getId());
    }

    public static long getThreadCpuTime(long threadId) {
        return CPU_TIME_ENABLED ? THREAD_BEAN.getThreadCpuTime(threadId) : -1;
    }

    public static long getThreadUserTime(long threadId) {
        return CPU_TIME_ENABLED ? THREAD_BEAN.getThreadUserTime(threadId) : -1;
    }
}

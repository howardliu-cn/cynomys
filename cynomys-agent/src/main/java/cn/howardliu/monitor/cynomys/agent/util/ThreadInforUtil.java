/**
 * @Probject Name: monitor_agent
 * @Path: com.wfj.monitor.utilThreadInforUtil.java
 * @Create By Jack
 * @Create In 2017年1月16日 下午4:43:19
 */
package cn.howardliu.monitor.cynomys.agent.util;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * @Class Name ThreadInforUtil
 * @Author Jack
 * @Create In 2017年1月16日
 */
public class ThreadInforUtil implements Serializable {

    /**
     * @Field long serialVersionUID
     */
    private static final long serialVersionUID = -392987809845207120L;

    private static final ThreadMXBean THREAD_BEAN = ManagementFactory.getThreadMXBean();
    private static final boolean CPU_TIME_ENABLED = THREAD_BEAN.isThreadCpuTimeSupported() && THREAD_BEAN
            .isThreadCpuTimeEnabled();

    public static long getCurrentThreadCpuTime() {
        return getThreadCpuTime(Thread.currentThread().getId());
    }

    private static long getThreadCpuTime(long threadId) {
        if (CPU_TIME_ENABLED) {
            // le coût de cette méthode se mesure à environ 0,6 microseconde
            return THREAD_BEAN.getThreadCpuTime(threadId);
        }
        return 0;
    }
}

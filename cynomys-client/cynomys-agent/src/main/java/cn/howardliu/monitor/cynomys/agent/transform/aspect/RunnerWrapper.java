package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.common.ThreadMXBeanUtils;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
public class RunnerWrapper {
    protected final long tid;
    protected final long startTime;
    protected final long startThreadCupTime;
    protected Throwable cause;

    public RunnerWrapper(long tid) {
        this.tid = tid;
        this.startTime = System.currentTimeMillis();
        this.startThreadCupTime = ThreadMXBeanUtils.getThreadCpuTime(tid);
    }

    public long getTid() {
        return tid;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStartThreadCupTime() {
        return startThreadCupTime;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RequestAspect.RequestDataWrapper that = (RequestAspect.RequestDataWrapper) o;
        return tid == that.tid;
    }

    @Override
    public int hashCode() {
        return (int) (tid ^ (tid >>> 32));
    }
}

package cn.howardliu.monitor.cynomys.agent.net.operator;

import cn.howardliu.monitor.cynomys.net.struct.Message;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public abstract class AbstractMonitorDataOperator implements IMonitorDataOperator {
    protected String name;
    protected int retryDelaySeconds = 10;
    protected int timeout = 5;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRetryDelaySeconds() {
        return retryDelaySeconds;
    }

    public void setRetryDelaySeconds(int retryDelaySeconds) {
        this.retryDelaySeconds = retryDelaySeconds;
    }

    public abstract void handleException(Throwable cause, Message message);

    public abstract boolean isActive();
}

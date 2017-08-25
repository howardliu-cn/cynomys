package cn.howardliu.monitor.cynomys.client.common;

import cn.howardliu.monitor.cynomys.net.NetHelper;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ClientConfig {
    private String clientIp = NetHelper.localAddress();
    private String instanceName = System.getProperty("cynomys.client.name", "DEFAULT");
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();

    public String buildClientId() {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder()
                .append(this.getClientIp())
                .append('@')
                .append(this.getInstanceName())
                .toString();
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }
}

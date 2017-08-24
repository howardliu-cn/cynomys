package cn.howardliu.monitor.cynomys.proxy.config;

import cn.howardliu.monitor.cynomys.net.NetHelper;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ServerConfig {
    private String serverName = NetHelper.localHostName();
    private int listenPort = ProxyConfig.PROXY_CONFIG.getPort();
    private int ctrlPort = ProxyConfig.PROXY_CONFIG.getCport();

    private int heartbeatActionThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;
    private int appInfoActionThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;
    private int requestInfoActionThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;
    private int sqlInfoActionThreadPoolNums = 16 + Runtime.getRuntime().availableProcessors() * 2;

    public String getServerName() {
        return serverName;
    }

    public ServerConfig setServerName(String serverName) {
        this.serverName = serverName;
        return this;
    }

    public int getListenPort() {
        return listenPort;
    }

    public ServerConfig setListenPort(int listenPort) {
        this.listenPort = listenPort;
        return this;
    }

    public int getCtrlPort() {
        return ctrlPort;
    }

    public ServerConfig setCtrlPort(int ctrlPort) {
        this.ctrlPort = ctrlPort;
        return this;
    }

    public int getHeartbeatActionThreadPoolNums() {
        return heartbeatActionThreadPoolNums;
    }

    public ServerConfig setHeartbeatActionThreadPoolNums(int heartbeatActionThreadPoolNums) {
        this.heartbeatActionThreadPoolNums = heartbeatActionThreadPoolNums;
        return this;
    }

    public int getAppInfoActionThreadPoolNums() {
        return appInfoActionThreadPoolNums;
    }

    public ServerConfig setAppInfoActionThreadPoolNums(int appInfoActionThreadPoolNums) {
        this.appInfoActionThreadPoolNums = appInfoActionThreadPoolNums;
        return this;
    }

    public int getRequestInfoActionThreadPoolNums() {
        return requestInfoActionThreadPoolNums;
    }

    public ServerConfig setRequestInfoActionThreadPoolNums(int requestInfoActionThreadPoolNums) {
        this.requestInfoActionThreadPoolNums = requestInfoActionThreadPoolNums;
        return this;
    }

    public int getSqlInfoActionThreadPoolNums() {
        return sqlInfoActionThreadPoolNums;
    }

    public ServerConfig setSqlInfoActionThreadPoolNums(int sqlInfoActionThreadPoolNums) {
        this.sqlInfoActionThreadPoolNums = sqlInfoActionThreadPoolNums;
        return this;
    }
}

package cn.howardliu.monitor.cynomys.net;

import java.util.concurrent.ThreadLocalRandom;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NettyClientConfig {
    private final String clientName;
    private int clientWorkerThreads = 4;
    private int connectTimeoutMillis = 3000;
    private int clientChannelMaxIdleTimeSeconds = 120;

    private int clientSocketSndBufSize = NettyNetConfig.socketSndbufSize;
    private int clientSocketRcvBufSize = NettyNetConfig.socketRcvbufSize;
    private int clientSocketMaxFrameLength = NettyNetConfig.socketMaxFrameLength;

    public NettyClientConfig() {
        this("Cynomys-Netty-Client-"
                + Long.toHexString(ThreadLocalRandom.current().nextLong() + System.currentTimeMillis()));
    }

    public NettyClientConfig(String clientName) {
        this.clientName = clientName;
    }

    public String getClientName() {
        return clientName;
    }

    public int getClientWorkerThreads() {
        return clientWorkerThreads;
    }

    public void setClientWorkerThreads(int clientWorkerThreads) {
        this.clientWorkerThreads = clientWorkerThreads;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getClientChannelMaxIdleTimeSeconds() {
        return clientChannelMaxIdleTimeSeconds;
    }

    public void setClientChannelMaxIdleTimeSeconds(int clientChannelMaxIdleTimeSeconds) {
        this.clientChannelMaxIdleTimeSeconds = clientChannelMaxIdleTimeSeconds;
    }

    public int getClientSocketSndBufSize() {
        return clientSocketSndBufSize;
    }

    public void setClientSocketSndBufSize(int clientSocketSndBufSize) {
        this.clientSocketSndBufSize = clientSocketSndBufSize;
    }

    public int getClientSocketRcvBufSize() {
        return clientSocketRcvBufSize;
    }

    public void setClientSocketRcvBufSize(int clientSocketRcvBufSize) {
        this.clientSocketRcvBufSize = clientSocketRcvBufSize;
    }

    public int getClientSocketMaxFrameLength() {
        return clientSocketMaxFrameLength;
    }

    public void setClientSocketMaxFrameLength(int clientSocketMaxFrameLength) {
        this.clientSocketMaxFrameLength = clientSocketMaxFrameLength;
    }
}

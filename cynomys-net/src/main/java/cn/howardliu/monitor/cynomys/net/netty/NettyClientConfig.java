package cn.howardliu.monitor.cynomys.net.netty;

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
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private int clientAsyncSemaphoreValue = NettyNetConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;
    private int connectTimeoutMillis = 3000;
    private int socketTimeoutMillis = 20000;
    private int clientChannelMaxIdleTimeSeconds = 120;

    private int clientSocketSndBufSize = NettyNetConfig.socketSndbufSize;
    private int clientSocketRcvBufSize = NettyNetConfig.socketRcvbufSize;
    private int clientSocketMaxFrameLength = NettyNetConfig.socketMaxFrameLength;

    private boolean clientCloseSocketIfTimeout = false;

    private int relinkMaxCount = 3;
    private int relinkDelayMillis = 2_000;

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

    public int getClientCallbackExecutorThreads() {
        return clientCallbackExecutorThreads;
    }

    public void setClientCallbackExecutorThreads(int clientCallbackExecutorThreads) {
        this.clientCallbackExecutorThreads = clientCallbackExecutorThreads;
    }

    public int getClientAsyncSemaphoreValue() {
        return clientAsyncSemaphoreValue;
    }

    public void setClientAsyncSemaphoreValue(int clientAsyncSemaphoreValue) {
        this.clientAsyncSemaphoreValue = clientAsyncSemaphoreValue;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public void setConnectTimeoutMillis(int connectTimeoutMillis) {
        this.connectTimeoutMillis = connectTimeoutMillis;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public void setSocketTimeoutMillis(int socketTimeoutMillis) {
        this.socketTimeoutMillis = socketTimeoutMillis;
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

    public boolean isClientCloseSocketIfTimeout() {
        return clientCloseSocketIfTimeout;
    }

    public void setClientCloseSocketIfTimeout(boolean clientCloseSocketIfTimeout) {
        this.clientCloseSocketIfTimeout = clientCloseSocketIfTimeout;
    }

    public int getRelinkMaxCount() {
        return relinkMaxCount;
    }

    public void setRelinkMaxCount(int relinkMaxCount) {
        this.relinkMaxCount = relinkMaxCount;
    }

    public int getRelinkDelayMillis() {
        return relinkDelayMillis;
    }

    public void setRelinkDelayMillis(int relinkDelayMillis) {
        this.relinkDelayMillis = relinkDelayMillis;
    }
}

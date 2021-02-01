package cn.howardliu.monitor.cynomys.net.netty;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.ThreadLocalRandom;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
@Getter
@Setter
public class NettyClientConfig {
    private final String clientName;
    private int clientWorkerThreads = 4;
    private int clientCallbackExecutorThreads = Runtime.getRuntime().availableProcessors();
    private int clientAsyncSemaphoreValue = NettyNetConfig.CLIENT_ASYNC_SEMAPHORE_VALUE;
    private int connectTimeoutMillis = 3000;
    private int socketTimeoutMillis = 20000;
    private int clientChannelMaxIdleTimeSeconds = 10;

    private int clientSocketSndBufSize = NettyNetConfig.socketSndbufSize;
    private int clientSocketRcvBufSize = NettyNetConfig.socketRcvbufSize;
    private int clientSocketMaxFrameLength = NettyNetConfig.socketMaxFrameLength;

    private boolean clientCloseSocketIfTimeout = false;

    private int relinkMaxCount = 3;
    private int relinkDelayMillis = 2_000;

    public NettyClientConfig() {
        this("Cynomys-Netty-Client-" + Long.toHexString(ThreadLocalRandom.current().nextLong() + System.currentTimeMillis()));
    }

    public NettyClientConfig(String clientName) {
        this.clientName = clientName;
    }
}

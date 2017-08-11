package cn.howardliu.monitor.cynomys.net.netty;

import static java.lang.Integer.parseInt;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
final class NettyNetConfig {
    public static final String CYNOMYS_NET_SOCKET_SNDBUF_SIZE = "cynomys.net.socket.sndbuf.size";
    public static final String CYNOMYS_NET_SOCKET_RCVBUF_SIZE = "cynomys.net.socket.rcvbuf.size";
    public static final String CYNOMYS_NET_SOCKET_MAX_FRAME_LENGTH = "cynomys.net.socket.max.frame.length";
    public static final String CYNOMYS_NET_CLIENT_ASYNC_SEMAPHORE_VALUE = "cynomys.net.clientAsyncSemaphoreValue";

    public static final int CLIENT_ASYNC_SEMAPHORE_VALUE =
            parseInt(System.getProperty(CYNOMYS_NET_CLIENT_ASYNC_SEMAPHORE_VALUE, "65535"));

    static int socketSndbufSize = parseInt(System.getProperty(CYNOMYS_NET_SOCKET_SNDBUF_SIZE, "65535"));
    static int socketRcvbufSize = parseInt(System.getProperty(CYNOMYS_NET_SOCKET_RCVBUF_SIZE, "65535"));
    static int socketMaxFrameLength = parseInt(System.getProperty(CYNOMYS_NET_SOCKET_MAX_FRAME_LENGTH,
            (10 * 1024 * 1024) + ""));

    private NettyNetConfig() {
    }
}

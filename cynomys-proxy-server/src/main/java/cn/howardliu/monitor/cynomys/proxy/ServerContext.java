package cn.howardliu.monitor.cynomys.proxy;

import cn.howardliu.monitor.cynomys.proxy.server.ProxyServer;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ServerContext {
    private static final Object lock = new Object();
    private static ServerContext serverContext = null;
    private final ProxyServer proxyServer;

    private ServerContext(ProxyServer proxyServer) {
        this.proxyServer = proxyServer;
    }

    public static ServerContext getInstance(ProxyServer proxyServer) {
        if (proxyServer == null) {
            throw new IllegalArgumentException("proxyServer cannot be null");
        }
        synchronized (lock) {
            if (serverContext == null) {
                serverContext = new ServerContext(proxyServer);
            } else {
                if (!(serverContext.getProxyServer() == proxyServer
                        || serverContext.getProxyServer().equals(proxyServer))) {
                    throw new IllegalArgumentException("ServerContext is already be init use different proxyServer");
                }
            }
        }
        return serverContext;
    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }
}

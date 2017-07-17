package cn.howardliu.monitor.cynomys.proxy;

import cn.howardliu.monitor.cynomys.proxy.config.ProxyConfig;
import cn.howardliu.monitor.cynomys.proxy.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class CynomysProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(CynomysProxyServer.class);

    public static void main(String[] args) {
        int port = ProxyConfig.INSTANCE.getPort();
        int cport = ProxyConfig.INSTANCE.getCport();
        ProxyServer proxyServer = new ProxyServer(port, cport);
        proxyServer.startup();
    }
}

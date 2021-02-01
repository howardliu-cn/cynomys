package cn.howardliu.monitor.cynomys.proxy;

import cn.howardliu.monitor.cynomys.proxy.config.ServerConfig;
import cn.howardliu.monitor.cynomys.proxy.server.ProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;

import static cn.howardliu.monitor.cynomys.proxy.Constants.COMMAND_SERVER_CTRL_STOP;
import static cn.howardliu.monitor.cynomys.proxy.config.ProxyConfig.PROXY_CONFIG;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public enum CynomysProxyServer {
    DAEMON;

    private static final Logger logger = LoggerFactory.getLogger(CynomysProxyServer.class);

    public static void main(String[] args) {
        try {
            String command = "start";
            if (args.length > 0) {
                command = args[0];
            }
            if ("start".equals(command)) {
                DAEMON.start();
            } else if ("stop".equals(command)) {
                DAEMON.stop();
            }
        } catch (Throwable t) {
            logger.error("got an exception when run input command", t);
            System.exit(1);
        }

    }

    public void start() {
        ProxyServer proxyServer = new ProxyServer(new ServerConfig());
        proxyServer.initialize();
        proxyServer.registProcessor();
        proxyServer.startup();
    }

    public void stop() {
        try (Socket socket = new Socket("localhost", PROXY_CONFIG.getCport());
             PrintWriter w = new PrintWriter(socket.getOutputStream())) {
            w.println(COMMAND_SERVER_CTRL_STOP);
            w.flush();
        } catch (ConnectException ce) {
            logger.error("CynomysProxyServer.stopServer.connectException: address=localhost, port={}",
                    PROXY_CONFIG.getCport());
            logger.error("CynomysProxyServer.stop: ", ce);
            System.exit(1);
        } catch (IOException e) {
            logger.error("CynomysProxyServer.stop: ", e);
            System.exit(1);
        }
    }
}

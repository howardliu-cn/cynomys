package cn.howardliu.monitor.cynomys.proxy.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static cn.howardliu.monitor.cynomys.proxy.Constants.COMMAND_SERVER_CTRL_STOP;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(AbstractServer.class);
    protected int port;
    protected int cport;
    private volatile boolean isListen = true;

    public AbstractServer(int port, int cport) {
        this.port = checkPort(port);
        this.cport = checkPort(cport);
    }

    private int checkPort(int port) {
        if (port < 0 || port > 0xFFFF) {
            throw new IllegalArgumentException("port out of range(0-65535]: " + port);
        }
        return port;
    }

    protected void ctrl() {
        Thread t = new Thread(() -> {
            logger.info("server listen to control port {}", cport);
            try (ServerSocket ss = new ServerSocket(cport)) {
                while (isListen) {
                    Socket s = ss.accept();
                    Scanner sc = new Scanner(s.getInputStream());
                    String command = sc.nextLine();
                    if (COMMAND_SERVER_CTRL_STOP.equals(command)) {
                        shutdownGracefully();
                    } else {
                        logger.warn("command \"{}\" not recognized", command);
                    }
                }
                logger.info("server listen to control port {} STOPPED!", cport);
            } catch (Exception e) {
                logger.error("listen to control port {} failed!", cport, e);
                shutdownGracefully();
            }
        });
        t.setDaemon(true);
        t.setName("proxy-server-ctrl-thread");
        t.start();
    }

    private void shutdownGracefully() {
        isListen = false;
        shutdown();
    }

    protected abstract void startup();

    protected abstract void shutdown();

    public int getPort() {
        return port;
    }

    public int getCport() {
        return cport;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractServer that = (AbstractServer) o;
        return port == that.port && cport == that.cport;
    }

    @Override
    public int hashCode() {
        int result = port;
        result = 31 * result + cport;
        return result;
    }
}

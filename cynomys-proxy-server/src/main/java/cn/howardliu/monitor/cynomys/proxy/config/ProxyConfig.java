package cn.howardliu.monitor.cynomys.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.howardliu.monitor.cynomys.proxy.Constants.KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT;
import static cn.howardliu.monitor.cynomys.proxy.Constants.KEY_CYNOMYS_PROXY_SERVER_PORT;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public enum ProxyConfig {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ProxyConfig.class);
    private static final int port;
    private static final int cport;

    static {
        int _port = 7911;
        int _cport = 52700;
        // TODO be sure config file path and load method
        try (InputStream inputStream = ProxyConfig.class.getResourceAsStream("/env/cynomys-config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            if (properties.containsKey(KEY_CYNOMYS_PROXY_SERVER_PORT)) {
                _port = Integer.valueOf(properties.getProperty(KEY_CYNOMYS_PROXY_SERVER_PORT));
            }
            String paramServerPort = System.getProperty(KEY_CYNOMYS_PROXY_SERVER_PORT);
            if (paramServerPort != null) {
                _port = Integer.valueOf(paramServerPort);
            }

            if (properties.containsKey(KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT)) {
                _cport = Integer.valueOf(properties.getProperty(KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT));
            }
            String paramServerCtrlPort = System.getProperty(KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT);
            if (paramServerCtrlPort != null) {
                _cport = Integer.valueOf(paramServerCtrlPort);
            }
        } catch (IOException e) {
            logger.error("Cannot load config properties in classpath", e);
            System.exit(1);
        }
        port = _port;
        cport = _cport;
    }

    public int getPort() {
        return port;
    }

    public int getCport() {
        return cport;
    }
}

package cn.howardliu.monitor.cynomys.proxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.howardliu.monitor.cynomys.proxy.Constants.*;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public enum ProxyConfig {
    PROXY_CONFIG;

    private static final Logger logger = LoggerFactory.getLogger(ProxyConfig.class);
    private static final int port;
    private static final int cport;
    private static final int maxFrameLength;
    private static final int timeoutSeconds;

    static {
        int _port = 7911;
        int _cport = 52700;
        int _maxFrameLength = 1024 * 1024 * 100;
        int _timeoutSeconds = 50;
        // TODO be sure config file path and load method
        try (InputStream inputStream = ProxyConfig.class.getResourceAsStream("/conf/cynomys-config.properties")) {
            Properties properties = new Properties();
            properties.load(inputStream);

            _port = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_PORT, _port);
            _cport = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT, _cport);
            _maxFrameLength = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_MAX_FRAME_LENGTH,
                    _maxFrameLength);
            _timeoutSeconds = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_TIMEOUT_SECONDS,
                    _timeoutSeconds);
        } catch (IOException e) {
            logger.error("Cannot load config properties in classpath", e);
            System.exit(1);
        }
        port = _port;
        cport = _cport;
        maxFrameLength = _maxFrameLength;
        timeoutSeconds = _timeoutSeconds;
    }

    private static int readFromConfigAndProperty(Properties properties, String key, int defaultValue) {
        if (properties.containsKey(key)) {
            defaultValue = Integer.valueOf(properties.getProperty(key));
        }
        String param = System.getProperty(key);
        // TODO check the String value is digital
        if (param != null) {
            defaultValue = Integer.valueOf(param);
        }
        return defaultValue;
    }

    public int getPort() {
        return port;
    }

    public int getCport() {
        return cport;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}

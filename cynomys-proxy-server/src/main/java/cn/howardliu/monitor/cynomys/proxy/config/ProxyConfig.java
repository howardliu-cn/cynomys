package cn.howardliu.monitor.cynomys.proxy.config;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static cn.howardliu.monitor.cynomys.common.Constant.CYNOMYS_HOME;
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

    private final Logger logger = LoggerFactory.getLogger(ProxyConfig.class);
    private final int port;
    private final int cport;
    private final int maxFrameLength;
    private final int timeoutSeconds;

    ProxyConfig() {
        int _port = 7911;
        int _cport = 52700;
        int _maxFrameLength = 1024 * 1024 * 100;
        int _timeoutSeconds = 50;
        try (InputStream inputStream = FileUtils
                .openInputStream(new File(CYNOMYS_HOME + "/conf/cynomys-config.properties"))) {
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

    private int readFromConfigAndProperty(Properties properties, String key, int defaultValue) {
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

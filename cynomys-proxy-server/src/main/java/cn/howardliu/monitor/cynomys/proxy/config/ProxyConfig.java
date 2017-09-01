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
        int thePort = 7911;
        int theCport = 52700;
        int theMaxFrameLength = 1024 * 1024 * 100;
        int theTimeoutSeconds = 50;
        try (InputStream inputStream = FileUtils
                .openInputStream(new File(CYNOMYS_HOME + "/conf/cynomys-config.properties"))) {
            Properties properties = new Properties();
            properties.load(inputStream);

            thePort = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_PORT, thePort);
            theCport = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_CTRL_PORT, theCport);
            theMaxFrameLength = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_MAX_FRAME_LENGTH,
                    theMaxFrameLength);
            theTimeoutSeconds = readFromConfigAndProperty(properties, KEY_CYNOMYS_PROXY_SERVER_TIMEOUT_SECONDS,
                    theTimeoutSeconds);
        } catch (IOException e) {
            logger.error("Cannot load config properties in classpath", e);
            System.exit(1);
        }
        port = thePort;
        cport = theCport;
        maxFrameLength = theMaxFrameLength;
        timeoutSeconds = theTimeoutSeconds;
    }

    private int readFromConfigAndProperty(Properties properties, String key, int defaultValue) {
        int theDefaultValue = defaultValue;
        if (properties.containsKey(key)) {
            theDefaultValue = Integer.valueOf(properties.getProperty(key));
        }
        String param = System.getProperty(key);
        // TODO check the String value is digital
        if (param != null) {
            theDefaultValue = Integer.valueOf(param);
        }
        return theDefaultValue;
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

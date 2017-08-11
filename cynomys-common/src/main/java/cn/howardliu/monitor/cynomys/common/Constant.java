package cn.howardliu.monitor.cynomys.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.UUID;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
// TODO server or client, need to check and notify
public final class Constant {
    public static final String VERSION_KEY = "cynomys.net.version";

    public static final int CRC_CODE = 0x0000_0_0_1;
    public static final String THIS_TAG;
    public static final String HEADER_SERVER_TAG = "Server-Tag";

    public static final boolean IS_DEBUG;
    public static final String SYS_NAME;
    public static final String SYS_CODE;

    private static final Logger logger = LoggerFactory.getLogger(Constant.class);
    private static final String DEFAULT_MONITOR_PROPERTIES_FILE = "/conf/default-cynomys-monitor.properties";
    private static final String CUSTOM_MONITOR_PROPERTIES_FILE = "/cynomys-monitor.properties";

    static {
        THIS_TAG = UUID.randomUUID().toString();

        boolean _isDebug = Boolean.TRUE;
        String sysName = "cynomys-monitor-project-default-name";
        String sysCode = "000";
        InputStream defaultIn = null;
        InputStream customIn = null;
        try {
            defaultIn = Constant.class.getResourceAsStream(DEFAULT_MONITOR_PROPERTIES_FILE);
            customIn = Constant.class.getResourceAsStream(CUSTOM_MONITOR_PROPERTIES_FILE);

            Properties properties = new Properties();
            if (defaultIn != null) {
                properties.load(defaultIn);
            }
            if (customIn != null) {
                properties.load(customIn);
            }
            properties.putAll(System.getProperties());

            _isDebug = Boolean.valueOf(properties.getProperty("system.setting.monitor.isDebug"));
            sysName = properties.getProperty("system.setting.context-name");
            sysCode = properties.getProperty("system.setting.context-code");
        } catch (IOException e) {
            logger.error("got an exception when load config file", e);
        } finally {
            close(defaultIn);
            close(customIn);
        }
        IS_DEBUG = _isDebug;
        SYS_NAME = sysName;
        SYS_CODE = sysCode;
    }

    private Constant() {
    }

    private static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                logger.error("got an exception when close Closeable object", e);
            }
        }
    }
}

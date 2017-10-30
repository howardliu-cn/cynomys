package cn.howardliu.monitor.cynomys.common;

import java.util.UUID;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class Constant {
    public static final String VERSION_KEY = "cynomys.net.version";

    public static final int CRC_CODE = CynomysVersion.CURRENT_VERSION_CODE;

    public static final String THIS_TAG;
    public static final String HEADER_SERVER_TAG = "Server-Tag";

    public static final String LOCAL_IP_ADDRESS = "127.0.0.1";
    public static final String LOCAL_HOST_NAME = "localhost";

    public static final String UNKNOWN_SERVER_NAME = "unknown";
    public static final String UNKNOWN_SERVER_VERSION = "unknown";

    private static final String CYNOMYS_HOME_ENV = "CYNOMYS_HOME";
    private static final String CYNOMYS_HOME_PROPERTY = "cynomys.home";
    public static final String CYNOMYS_HOME = System
            .getProperty(CYNOMYS_HOME_PROPERTY, System.getenv(CYNOMYS_HOME_ENV));

    static {
        THIS_TAG = UUID.randomUUID().toString();
    }

    private Constant() {
    }
}

package cn.howardliu.monitor.cynomys.common;

import javax.servlet.ServletContext;
import java.util.UUID;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class Constant {
    public static final String CYNOMYS_HOME_ENV = "CYNOMYS_HOME";
    public static final String CYNOMYS_HOME_PROPERTY = "cynomys.home";

    public static final String CYNOMYS_HOME = System
            .getProperty(CYNOMYS_HOME_PROPERTY, System.getenv(CYNOMYS_HOME_ENV));

    public static final String VERSION_KEY = "cynomys.net.version";
    public static final int CRC_CODE = CynomysVersion.CURRENT_VERSION_CODE;
    public static final String THIS_TAG;
    public static final String HEADER_SERVER_TAG = "Server-Tag";

    public static final String UNKNOWN_SERVER_NAME = "unknown";
    public static final String UNKNOWN_SERVER_VERSION = "unknown";

    public static volatile boolean isDebug = Boolean.TRUE;
    public static String sysName = "cynomys-monitor-project-default-name";
    public static String sysCode = "000";
    public static String sysDesc = "'{'\"name\":\"{0}\",\"code\":\"{1}\",\"version\":\"1.0\",\"desc\":\"https://github.com/howardliu-cn/cynomys\",\"status\":\"{2}\"'}'";

    public static volatile String serverList = "127.0.0.1:7911";
    public static volatile boolean noFlag = true;
    public static volatile int serverPort = 8080;
    public static ServletContext servletContext = null;

    static {
        THIS_TAG = UUID.randomUUID().toString();
    }

    private Constant() {
    }
}

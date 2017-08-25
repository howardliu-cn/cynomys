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
    public static final String VERSION_KEY = "cynomys.net.version";
    public static final int CRC_CODE = CynomysVersion.CURRENT_VERSION_CODE;
    public static final String THIS_TAG;
    public static final String HEADER_SERVER_TAG = "Server-Tag";

    public static volatile boolean IS_DEBUG = Boolean.TRUE;
    public static String SYS_NAME = "cynomys-monitor-project-default-name";
    public static String SYS_CODE = "000";
    public static String SYS_DESC = "'{'\"name\":\"{0}\",\"code\":\"{1}\",\"version\":\"1.0\",\"desc\":\"https://github.com/howardliu-cn/cynomys\",\"status\":\"{2}\"'}'";
    public static volatile String SERVER_LIST = "127.0.0.1:7911";

    public static volatile boolean NO_FLAG = true;
    public static volatile int SERVER_PORT = 8080;
    public static ServletContext SERVLET_CONTEXT = null;

    static {
        THIS_TAG = UUID.randomUUID().toString();
    }

    private Constant() {
    }
}

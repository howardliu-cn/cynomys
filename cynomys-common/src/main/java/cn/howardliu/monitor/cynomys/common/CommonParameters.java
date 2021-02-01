package cn.howardliu.monitor.cynomys.common;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.util.*;

/**
 * <br>created at 17-9-1
 *
 * @author liuxh
 * @since 0.0.1
 */
public final class CommonParameters {
    public static volatile Set<String> SPRINGBOOT_INIT_ARGS = Collections.synchronizedSet(new HashSet<String>());
    @SuppressWarnings("WeakerAccess")
    public static volatile ServletContext servletContext = null;
    private static volatile boolean debugMode = Boolean.TRUE;
    private static String sysName = "cynomys-monitor-project-default-name";
    private static String sysCode = "000";
    private static String sysDesc = "'{'\"name\":\"{0}\",\"code\":\"{1}\",\"version\":\"1.0\",\"desc\":\"https://github.com/howardliu-cn/cynomys\",\"status\":\"{2}\"'}'";
    private static String sysVersion = System.currentTimeMillis() + "";
    private static volatile String serverList = Constant.LOCAL_IP_ADDRESS + ":7911";
    private static volatile boolean noFlag = true;
    private static volatile int serverPort = 8080;
    private static Map<String, DataSource> dataSources = Collections.synchronizedMap(new HashMap<String, DataSource>());
    private static volatile boolean injectDataSources = false;

    public static boolean isDebugMode() {
        return debugMode;
    }

    public static void setDebugMode(boolean debugMode) {
        CommonParameters.debugMode = debugMode;
    }

    public static String getSysName() {
        return sysName;
    }

    public static void setSysName(String sysName) {
        CommonParameters.sysName = sysName;
    }

    public static String getSysCode() {
        return sysCode;
    }

    public static void setSysCode(String sysCode) {
        CommonParameters.sysCode = sysCode;
    }

    public static String getSysDesc() {
        return sysDesc;
    }

    public static void setSysDesc(String sysDesc) {
        CommonParameters.sysDesc = sysDesc;
    }

    public static String getSysVersion() {
        return sysVersion;
    }

    public static void setSysVersion(final String sysVersion) {
        CommonParameters.sysVersion = sysVersion;
    }

    public static String getServerList() {
        return serverList;
    }

    public static void setServerList(String serverList) {
        CommonParameters.serverList = serverList;
    }

    public static boolean isNoFlag() {
        return noFlag;
    }

    public static void setNoFlag(boolean noFlag) {
        CommonParameters.noFlag = noFlag;
    }

    public static int getServerPort() {
        return serverPort;
    }

    public static void setServerPort(int serverPort) {
        CommonParameters.serverPort = serverPort;
    }

    public static ServletContext getServletContext() {
        return servletContext;
    }

    public static void setServletContext(ServletContext servletContext) {
        CommonParameters.servletContext = servletContext;
    }

    public static Map<String, DataSource> getDataSources() {
        return dataSources;
    }

    public static void setDataSources(Map<String, DataSource> dataSources) {
        CommonParameters.dataSources = dataSources;
    }

    public static boolean isInjectDataSources() {
        return injectDataSources;
    }

    public static void setInjectDataSources(boolean injectDataSources) {
        CommonParameters.injectDataSources = injectDataSources;
    }
}

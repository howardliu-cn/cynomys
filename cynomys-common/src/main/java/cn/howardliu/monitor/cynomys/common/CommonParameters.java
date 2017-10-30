package cn.howardliu.monitor.cynomys.common;

import javax.servlet.ServletContext;

import static cn.howardliu.monitor.cynomys.common.Constant.LOCAL_IP_ADDRESS;

/**
 * <br>created at 17-9-1
 *
 * @author liuxh
 * @since 0.0.1
 */
public final class CommonParameters {
    private static volatile boolean debugMode = Boolean.TRUE;
    private static String sysName = "cynomys-monitor-project-default-name";
    private static String sysCode = "000";
    private static String sysDesc = "'{'\"name\":\"{0}\",\"code\":\"{1}\",\"version\":\"1.0\",\"desc\":\"https://github.com/howardliu-cn/cynomys\",\"status\":\"{2}\"'}'";

    private static volatile String serverList = LOCAL_IP_ADDRESS + ":7911";
    private static volatile boolean noFlag = true;
    private static volatile int serverPort = 8080;
    private static ServletContext servletContext = null;

    private CommonParameters() {
    }

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
}

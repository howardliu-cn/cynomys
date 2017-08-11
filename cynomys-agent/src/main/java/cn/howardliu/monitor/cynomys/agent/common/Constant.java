package cn.howardliu.monitor.cynomys.agent.common;

import javax.servlet.ServletContext;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Constant {
    public static final String DEFAULT_ENV_PROPERTIES_FILE = "/env/monitor-env-setting-default.properties";
    public static final String DEFAULT_MONITOR_PROPERTIES_FILE = "/env/default-wfj-monitor.properties";
    public static final String CUSTOM_MONITOR_PROPERTIES_FILE = "/wfj-monitor.properties";
    public static final String SYSTEM_SETTING_MONITOR_IS_DEBUG = "system.setting.monitor.isDebug";
    public static final String SYSTEM_SETTING_EXCLUDE_PACKAGE = "system.setting.exclude.package";
    public static final String SYSTEM_SETTING_INCLUDE_PACKAGE = "system.setting.include.package";
    public static final String SYSTEM_SETTING_EXCLUDE_CLASS_LOADER = "system.setting.exclude.ClassLoader";
    public static final String SYSTEM_SETTING_MONITOR_SERVERS = "system.setting.monitor.servers";

    public static Boolean IS_DEBUG = false;
    public static ServletContext SERVLET_CONTEXT = null;
    public static Integer SERVER_PORT = 8080;
    public static volatile boolean started = false;
}

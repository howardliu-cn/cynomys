package cn.howardliu.monitor.cynomys.client.common;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class Constant {
    public static final String SYSTEM_SETTING_MONITOR_IS_DEBUG = "system.setting.monitor.isDebug";
    public static final String SYSTEM_SETTING_CONTEXT_NAME = "system.setting.context-name";
    public static final String SYSTEM_SETTING_CONTEXT_CODE = "system.setting.context-code";
    public static final String SYSTEM_SETTING_CONTEXT_DESC = "system.setting.context-desc";
    public static final String SYSTEM_SETTING_MONITOR_SERVERS = "system.setting.monitor.servers";

    public static final String SYSTEM_SETTING_EXCLUDE_PACKAGE = "system.setting.exclude.package";
    public static final String SYSTEM_SETTING_INCLUDE_PACKAGE = "system.setting.include.package";
    public static final String SYSTEM_SETTING_EXCLUDE_CLASS_LOADER = "system.setting.exclude.ClassLoader";

    public static final String SYSTEM_SETTING_SERVER_PORT = "system.setting.server.port";
    public static final String SYSTEM_SETTING_SERVER_CONTEXT = "system.setting.server.context";

    public static volatile boolean started = false;

    private Constant() {
    }
}

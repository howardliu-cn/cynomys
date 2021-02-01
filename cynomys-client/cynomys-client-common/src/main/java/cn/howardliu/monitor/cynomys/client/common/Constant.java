package cn.howardliu.monitor.cynomys.client.common;

import javax.servlet.ServletContext;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class Constant {
    static final String SYSTEM_SETTING_MONITOR_IS_DEBUG = "system.setting.monitor.isDebug";
    static final String SYSTEM_SETTING_CONTEXT_NAME = "system.setting.context-name";
    static final String SYSTEM_SETTING_CONTEXT_CODE = "system.setting.context-code";
    static final String SYSTEM_SETTING_CONTEXT_DESC = "system.setting.context-desc";
    static final String SYSTEM_SETTING_MONITOR_SERVERS = "system.setting.monitor.servers";

    public static final String SYSTEM_SETTING_EXCLUDE_PACKAGE = "system.setting.exclude.package";
    public static final String SYSTEM_SETTING_INCLUDE_PACKAGE = "system.setting.include.package";
    public static final String SYSTEM_SETTING_EXCLUDE_CLASS_LOADER = "system.setting.exclude.ClassLoader";

    public static ServletContext servletContext = null;

    private Constant() {
    }
}

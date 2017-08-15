package cn.howardliu.monitor.cynomys.agent.conf;

import java.io.InputStream;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.*;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SystemPropertyConfig {
    private static PropertyAdapter _config = new PropertyAdapter();

    static {
        _config.add(DEFAULT_MONITOR_PROPERTIES_FILE);
    }

    public static void init() {
        InputStream in = SystemPropertyConfig.class.getResourceAsStream(CUSTOM_MONITOR_PROPERTIES_FILE);
        if (in != null) {
            init(CUSTOM_MONITOR_PROPERTIES_FILE);
        }
    }

    public static void init(String fileName) {
        _config.add(fileName);
        IS_DEBUG = SystemPropertyConfig.getBoolean(SYSTEM_SETTING_MONITOR_IS_DEBUG, true);
    }

    public static String getContextProperty(String name) {
        return _config.getContextProperty(name);
    }

    public static Boolean getBoolean(String name, Boolean defaultValue) {
        String v = getContextProperty(name);
        return v == null ? defaultValue : Boolean.valueOf(v);
    }

    public static String getContextProperty(String name, String defaultValue) {
        return _config.getContextProperty(name, defaultValue);
    }

    public static void setContextProperty(String name, String value) {
        _config.setContextProperty(name, value);
    }
}

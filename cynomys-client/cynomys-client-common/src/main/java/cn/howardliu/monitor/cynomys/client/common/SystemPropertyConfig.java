package cn.howardliu.monitor.cynomys.client.common;

import cn.howardliu.monitor.cynomys.common.Constant;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static cn.howardliu.monitor.cynomys.client.common.Constant.*;
import static org.apache.commons.lang3.SystemUtils.USER_HOME;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class SystemPropertyConfig {
    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyConfig.class);
    private static PropertyAdapter _config = new PropertyAdapter();

    private static String DEFAULT_MONITOR_PROPERTIES_FILE = "/conf/default-cynomys-monitor.properties";
    private static String SYS_CUSTOM_MONITOR_PROPERTIES_FILE = USER_HOME + "/.cynomys/cynomys-monitor.properties";
    private static String CURRENT_MONITOR_PROPERTIES_FILE = "cynomys-monitor.properties";
    private static String ATTRIBUTE_MONITOR_PROPERTIES_FILE = System.getProperty("cynomys-monitor.properties");

    private SystemPropertyConfig() {
    }

    public static void init() {
        init(null);
    }

    // 1. the config file is in jar:/conf/default-cynomys-monitor.properties
    // 2. the config file is in ~/.cynomys/cynomys-monitor.properties
    // 3. the config file is in current path: cynomys-monitor.properties
    // 3. the config file is javaagent argument: -javaagent:xxx.jar=/path/to/xxx.properties
    // 4. the config file is jvm parameter: -Dcynomys-monitor.properties=/path/to/xxx.properties
    // 5. the config properties load System.getProperties()
    public static void init(String agentArgs) {
        _config.add(DEFAULT_MONITOR_PROPERTIES_FILE);

        boolean extract1 = _config.addFile(SYS_CUSTOM_MONITOR_PROPERTIES_FILE);
        boolean extract2 = _config.addFile(CURRENT_MONITOR_PROPERTIES_FILE);
        boolean extract4 = _config.addFile(agentArgs);
        boolean extract3 = _config.addFile(ATTRIBUTE_MONITOR_PROPERTIES_FILE);

        _config.addAll(System.getProperties());

        loadConfig();

        if (!(extract1 || extract2 || extract3 || extract4)) {
            extractDefaultProperties();
        }
    }

    private static void loadConfig() {
        Constant.IS_DEBUG = getBoolean(SYSTEM_SETTING_MONITOR_IS_DEBUG, Constant.IS_DEBUG);
        Constant.SYS_NAME = getContextProperty(SYSTEM_SETTING_CONTEXT_NAME, Constant.SYS_NAME);
        Constant.SYS_CODE = getContextProperty(SYSTEM_SETTING_CONTEXT_CODE, Constant.SYS_CODE);
        Constant.SYS_DESC = getContextProperty(SYSTEM_SETTING_CONTEXT_DESC, Constant.SYS_DESC);
        Constant.SERVER_LIST = getContextProperty(SYSTEM_SETTING_MONITOR_SERVERS, Constant.SERVER_LIST);
    }

    private static void extractDefaultProperties() {
        InputStream in = SystemPropertyConfig.class.getResourceAsStream(DEFAULT_MONITOR_PROPERTIES_FILE);
        try {
            FileUtils.copyInputStreamToFile(in, new File(SYS_CUSTOM_MONITOR_PROPERTIES_FILE));
        } catch (IOException e) {
            logger.error("extract default properties file to {} exception", SYS_CUSTOM_MONITOR_PROPERTIES_FILE, e);
        } finally {
            IOUtils.closeQuietly(in);
        }
    }

    public static String getContextProperty(String name) {
        return _config.getContextProperty(name);
    }

    private static Boolean getBoolean(String name, Boolean defaultValue) {
        String v = getContextProperty(name);
        return v == null ? defaultValue : Boolean.valueOf(v);
    }

    private static String getContextProperty(String name, String defaultValue) {
        return _config.getContextProperty(name, defaultValue);
    }

    public static void setContextProperty(String name, String value) {
        _config.setContextProperty(name, value);
    }
}

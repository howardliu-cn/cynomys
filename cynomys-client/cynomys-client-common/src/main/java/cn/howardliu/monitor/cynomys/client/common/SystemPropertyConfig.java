package cn.howardliu.monitor.cynomys.client.common;

import cn.howardliu.monitor.cynomys.common.CommonParameters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
    private static final String DEFAULT_MONITOR_PROPERTIES_FILE = "/conf/default-cynomys-monitor.properties";
    private static final String SYS_CUSTOM_MONITOR_PROPERTIES_FILE = USER_HOME + "/.cynomys/cynomys-monitor.properties";
    private static final String CURRENT_MONITOR_PROPERTIES_FILE_IN_JAR = "/cynomys-monitor.properties";
    private static final String CURRENT_MONITOR_PROPERTIES_FILE = "cynomys-monitor.properties";
    private static final String ATTRIBUTE_MONITOR_PROPERTIES_FILE = System.getProperty("cynomys-monitor.properties");
    private static final String SYSTEM_SETTING_BIZ = "system.setting.biz.";
    private static final String SYSTEM_SETTING_ERR = "system.setting.err.";
    private static PropertiesAdapter thisConfig = new PropertiesAdapter();

    public static final Map<String, String> bizParams = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, String> errParams = Collections.synchronizedMap(new HashMap<>());

    private SystemPropertyConfig() {
    }

    public static void init() {
        init(null);
    }

    // 1. the config file is in jar:/conf/default-cynomys-monitor.properties
    // 2. the config file is in ~/.cynomys/cynomys-monitor.properties
    // 3. the config file is in jar: /cynomys-monitor.properties
    // 4. the config file is in current path: cynomys-monitor.properties
    // 5. the config file is javaagent argument: -javaagent:xxx.jar=/path/to/xxx.properties
    // 6. the config file is jvm parameter: -Dcynomys-monitor.properties=/path/to/xxx.properties
    // 7. the config properties load System.getProperties()
    public static void init(String agentArgs) {
        thisConfig.add(DEFAULT_MONITOR_PROPERTIES_FILE);

        boolean extract1 = thisConfig.addFile(SYS_CUSTOM_MONITOR_PROPERTIES_FILE);
        boolean extract2 = thisConfig.add(CURRENT_MONITOR_PROPERTIES_FILE_IN_JAR);
        boolean extract3 = thisConfig.addFile(CURRENT_MONITOR_PROPERTIES_FILE);
        boolean extract4 = thisConfig.addFile(agentArgs);
        boolean extract5 = thisConfig.addFile(ATTRIBUTE_MONITOR_PROPERTIES_FILE);

        thisConfig.addAll(System.getProperties());
        System.out.println("cynomys-monitor config : " + thisConfig);

        loadConfig();

        if (!(extract1 || extract2 || extract3 || extract4 || extract5)) {
            extractDefaultProperties();
        }

        loadExceptionDesc();
    }

    private static void loadExceptionDesc() {
        for (String key : thisConfig.defaults.stringPropertyNames()) {
            final String desc = thisConfig.defaults.getProperty(key);

            if (key.startsWith(SYSTEM_SETTING_BIZ) && key.length() > SYSTEM_SETTING_BIZ.length()) {
                final String bizCode = key.substring(SYSTEM_SETTING_BIZ.length());
                if (bizCode.matches("\\d{3}")) {
                    bizParams.put(bizCode, desc);
                }
            } else {
                if (key.startsWith(SYSTEM_SETTING_ERR) && key.length() > SYSTEM_SETTING_ERR.length()) {
                    final String errCode = key.substring(SYSTEM_SETTING_ERR.length());
                    if (errCode.matches("\\d{3}")) {
                        errParams.put(errCode, desc);
                    }
                }
            }
        }
    }

    private static void loadConfig() {
        CommonParameters.setDebugMode(getBoolean(Constant.SYSTEM_SETTING_MONITOR_IS_DEBUG, CommonParameters.isDebugMode()));
        CommonParameters.setSysName(getContextProperty(Constant.SYSTEM_SETTING_CONTEXT_NAME, CommonParameters.getSysName()));
        CommonParameters.setSysCode(getContextProperty(Constant.SYSTEM_SETTING_CONTEXT_CODE, CommonParameters.getSysCode()));
        CommonParameters.setSysDesc(getContextProperty(Constant.SYSTEM_SETTING_CONTEXT_DESC, CommonParameters.getSysDesc()));
        CommonParameters.setSysVersion(getContextProperty(Constant.SYSTEM_SETTING_CONTEXT_VERSION, CommonParameters.getSysVersion()));
        CommonParameters.setServerList(getContextProperty(Constant.SYSTEM_SETTING_MONITOR_SERVERS, CommonParameters.getServerList()));
    }

    private static void extractDefaultProperties() {
        logger.info("extract default properties file in ${user.home}/.cynomys/cynomys-monitor.properties");
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
        return thisConfig.getProperty(name);
    }

    private static Boolean getBoolean(String name, Boolean defaultValue) {
        String v = getContextProperty(name);
        return v == null ? defaultValue : Boolean.valueOf(v);
    }

    private static String getContextProperty(String name, String defaultValue) {
        return thisConfig.getProperty(name, defaultValue);
    }

    public static void setContextProperty(String name, String value) {
        thisConfig.setProperty(name, value);
    }
}

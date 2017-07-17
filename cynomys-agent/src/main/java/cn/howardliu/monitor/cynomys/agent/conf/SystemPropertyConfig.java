package cn.howardliu.monitor.cynomys.agent.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.*;

/**
 * @Class Name SystemPropertyConfig
 * @Author Jack
 * @Create In 2015年8月25日
 */
public class SystemPropertyConfig {
    private static Logger log = LoggerFactory.getLogger(SystemPropertyConfig.class);

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

    /**
     * 返回指定属性的值
     *
     * @param name 属性的Key
     * @return 值
     * @Methods Name getContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public static String getContextProperty(String name) {
        return _config.getContextProperty(name);
    }

    public static Boolean getBoolean(String name, Boolean defaultValue) {
        String v = getContextProperty(name);
        return v == null ? defaultValue : Boolean.valueOf(v);
    }

    /**
     * 返回指定属性的值
     *
     * @param name        属性的Key
     * @param defultValue 如果没找到的默认值
     * @return 值，如没有返回defultValue
     * @Methods Name getContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public static String getContextProperty(String name, String defultValue) {
        return _config.getContextProperty(name, defultValue);
    }

    /**
     * 设置属性值
     *
     * @param name  属性的Key
     * @param value 属性的值
     * @Methods Name setContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public static void setContextProperty(String name, String value) {
        _config.setContextProperty(name, value);
    }
}

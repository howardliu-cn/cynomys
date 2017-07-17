package cn.howardliu.monitor.cynomys.agent.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.DEFAULT_ENV_PROPERTIES_FILE;

/**
 * @Class Name PropertyContext
 * @Author Jack
 * @Create In 2015年8月25日
 */
public class EnvPropertyConfig {
    private static Logger log = LoggerFactory.getLogger(EnvPropertyConfig.class);
    private static PropertyAdapter _config = new PropertyAdapter();

    private static final String PROPERTY_CONTEXT_PATH_ENV = DEFAULT_ENV_PROPERTIES_FILE;

    public static void init() {
        _config.add(PROPERTY_CONTEXT_PATH_ENV);
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

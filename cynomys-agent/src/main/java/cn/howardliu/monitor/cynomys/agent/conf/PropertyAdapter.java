package cn.howardliu.monitor.cynomys.agent.conf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Properties;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class PropertyAdapter {
    private static Logger logger = LoggerFactory.getLogger(PropertyAdapter.class);
    protected Properties pc = new Properties();

    protected void add(String fileName) {
        try {
            Properties properties = new Properties();
            InputStream in = PropertyAdapter.class.getResourceAsStream(fileName);
            properties.load(in);
            pc.putAll(properties);
        } catch (IOException e) {
            logger.error("Customer Server Resource file did not find, please check again! Details: " + e.getMessage());
        }
    }

    /**
     * 返回指定属性的值
     *
     * @param name 属性的Key
     * @return 值
     * @Methods Name getContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public String getContextProperty(String name) {
        return pc.getProperty(name);
    }

    /**
     * 返回指定属性的值
     *
     * @param name         属性的Key
     * @param defaultValue 如果没找到的默认值
     * @return 值，如没有返回defultValue
     * @Methods Name getContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public String getContextProperty(String name, String defaultValue) {
        return pc.getProperty(name, defaultValue);
    }

    public Boolean getBoolean(String name, Boolean defaultValue) {
        return pc.contains(name) ? Boolean.valueOf(getContextProperty(name)) : defaultValue;
    }

    /**
     * 设置属性值
     *
     * @param name  属性的Key
     * @param value 属性的值
     * @Methods Name setContextProperty
     * @Create In 2015年8月25日 By Jack
     */
    public void setContextProperty(String name, String value) {
        pc.setProperty(name, value);
    }

    /**
     * 格式化字符串
     *
     * @param source  待替换字符串 ｛0｝起
     * @param targets 需要替换的内容
     * @return String
     * @Methods Name formatter
     * @Create In 2015年8月25日 By Jack
     */
    public static String formatter(String source, Object[] targets) {
        return MessageFormat.format(source, targets);
    }
}

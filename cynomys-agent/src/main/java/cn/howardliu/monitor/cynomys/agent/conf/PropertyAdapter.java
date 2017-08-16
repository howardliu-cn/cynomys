package cn.howardliu.monitor.cynomys.agent.conf;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
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

    protected boolean addFile(String filePath) {
        if (filePath == null || StringUtils.isBlank(filePath)) {
            return false;
        }
        try {
            File file = new File(filePath);
            if (!file.exists() || file.isDirectory() || !file.isFile() || !file.canRead()) {
                throw new IllegalArgumentException("Config file cannot read!");
            }
            Properties properties = new Properties();
            properties.load(new FileInputStream(file));
            pc.putAll(properties);
            return true;
        } catch (Exception e) {
            logger.error("Load config file [{}] exception, please check again! Details: {}", filePath, e.getMessage());
        }
        return false;
    }

    protected void addAll(Properties properties) {
        if (properties == null) {
            return;
        }
        pc.putAll(properties);
    }

    public String getContextProperty(String name) {
        return pc.getProperty(name);
    }

    public String getContextProperty(String name, String defaultValue) {
        return pc.getProperty(name, defaultValue);
    }

    public void setContextProperty(String name, String value) {
        pc.setProperty(name, value);
    }

    @Override
    public String toString() {
        return pc.toString();
    }
}

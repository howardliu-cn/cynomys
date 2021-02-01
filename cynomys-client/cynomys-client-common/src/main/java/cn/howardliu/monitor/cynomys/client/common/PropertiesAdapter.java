package cn.howardliu.monitor.cynomys.client.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class PropertiesAdapter {
    private static Logger logger = LoggerFactory.getLogger(PropertiesAdapter.class);
    protected Properties defaults = new Properties();

    protected boolean add(final String fileName) {
        try (InputStream in = PropertiesAdapter.class.getResourceAsStream(fileName)) {
            Properties properties = new Properties();
            properties.load(in);
            defaults.putAll(properties);
            logger.error("Load Customer Server Resource file [" + fileName + "] read successfully");
            return true;
        } catch (Throwable e) {
            logger.error("Load Customer Server Resource file [" + fileName + "] exception: " + e.getMessage());
        }
        return false;
    }

    protected boolean addFile(final String filePath) {
        if (filePath == null || StringUtils.isBlank(filePath)) {
            return false;
        }
        FileInputStream in = null;
        try {
            in = FileUtils.openInputStream(new File(filePath));
            Properties properties = new Properties();
            properties.load(in);
            defaults.putAll(properties);
            logger.debug("Load config file [" + filePath + "] successfully");
            return true;
        } catch (Throwable e) {
            IOUtils.closeQuietly(in);
            logger.debug("Load config file [" + filePath + "] exception: " + e.getMessage());
        }
        return false;
    }

    protected void addAll(final Properties properties) {
        if (properties == null) {
            return;
        }
        defaults.putAll(properties);
    }

    public String getProperty(String name) {
        return defaults.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return defaults.getProperty(name, defaultValue);
    }

    public void setProperty(String name, String value) {
        defaults.setProperty(name, value);
    }

    @Override
    public String toString() {
        return defaults.toString();
    }
}

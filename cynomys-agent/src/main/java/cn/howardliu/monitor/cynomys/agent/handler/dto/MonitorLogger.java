/**
 * @Probject Name: monitor_agent
 * @Path: com.wfj.monitor.handler.factoryMonitorLogger.java
 * @Create By Jack
 * @Create In 2017年1月16日 下午3:22:37
 */
package cn.howardliu.monitor.cynomys.agent.handler.dto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 带格式的日志记录器
 *
 * @Class Name MonitorLogger
 * @Author Jack
 * @Create In 2017年1月16日
 */
public class MonitorLogger {

    private Logger logger;

    public <T> MonitorLogger(Class<T> clazz) {
        logger = LoggerFactory.getLogger(clazz);
    }

    public MonitorLogger(String clazzName) {
        logger = LoggerFactory.getLogger(clazzName);
    }

    public MonitorLogger() {
        logger = LoggerFactory.getLogger("DefaultMonitorLoger");
    }

    public void error(String msg, Throwable e) {
        logger.error("************Monitor Logger Error Begin************");
        logger.error(msg);
        logger.error("The Details:");
        logger.error(e.getStackTrace().toString());
        logger.error("************Monitor Logger Error End************");
    }

    public void error(String msg) {
        logger.error("************Monitor Logger Error Begin************");
        logger.error(msg);
        logger.error("************Monitor Logger Error End************");
    }

    public void error(Throwable e) {
        error(e.getStackTrace().toString());
    }

    public void warn(String msg, Throwable e) {
        logger.warn("************Monitor Logger Warn Begin************");
        logger.warn(msg);
        logger.warn("The Details:");
        logger.warn("************Monitor Logger Warn End************");
    }

    public void warn(String msg) {
        logger.warn("************Monitor Logger Warn Begin************");
        logger.warn(msg);
        logger.warn("************Monitor Logger Warn End************");
    }

    public void warn(Throwable e) {
        warn(e.getStackTrace().toString());
    }

    public void info(String msg, Throwable e) {
        logger.info("************Monitor Logger Info Begin************");
        logger.info(msg);
        logger.info("The Details:");
        logger.info(e.getStackTrace().toString());
        logger.info("************Monitor Logger Info End************");
    }

    public void info(Throwable e) {
        info(e.getStackTrace().toString());
    }

    public void info(String msg) {
        logger.info("************Monitor Logger Info Begin************");
        logger.info(msg);
        logger.info("************Monitor Logger Info End************");
    }

    public void debug(String msg, Throwable e) {
        logger.debug("************Monitor Logger Info Begin************");
        logger.debug(msg);
        logger.debug("The Details:");
        logger.debug(e.getStackTrace().toString());
        logger.debug("************Monitor Logger Info End************");
    }

    public void debug(Throwable e) {
        debug(e.getStackTrace().toString());
    }

    public void debug(String msg) {
        logger.debug("************Monitor Logger Info Begin************");
        logger.debug(msg);
        logger.debug("************Monitor Logger Info End************");
    }
}

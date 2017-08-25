package cn.howardliu.monitor.cynomys.spring.web;

import cn.howardliu.gear.monitor.tomcat.TomcatInfoUtils;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

import static cn.howardliu.monitor.cynomys.common.Constant.SERVER_PORT;

/**
 * <br>created at 17-8-17
 *
 * @author liuxh
 * @since 0.0.1
 */
@Component
public class StatedListener implements ServletContextAware, ApplicationListener<ContextRefreshedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(StatedListener.class);

    @Override
    public void setServletContext(ServletContext servletContext) {
        Constant.SERVLET_CONTEXT = servletContext;
        try {
            if (TomcatInfoUtils.SERVER_IS_TOMCAT) {
                Constant.SERVER_PORT = TomcatInfoUtils.getPort();
            } else {
                Constant.SERVER_PORT = Integer.valueOf(System.getProperty("server.port", SERVER_PORT + ""));
            }
        } catch (Exception e) {
            logger.error("got server port exception", e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("the server info: "
                    + "\n\t listen port = " + Constant.SERVER_PORT
                    + "\n\t servlet info = " + Constant.SERVLET_CONTEXT.getServerInfo()
                    + "\n\t server major version = " + Constant.SERVLET_CONTEXT.getMajorVersion()
                    + "\n\t server minor version = " + Constant.SERVLET_CONTEXT.getMinorVersion()
                    + "\n\t server context name = " + Constant.SERVLET_CONTEXT.getServletContextName()
            );
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LaunchLatch.STARTED.start();
    }
}

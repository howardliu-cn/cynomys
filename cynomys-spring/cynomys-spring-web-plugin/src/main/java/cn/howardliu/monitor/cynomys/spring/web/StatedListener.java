package cn.howardliu.monitor.cynomys.spring.web;

import cn.howardliu.gear.monitor.tomcat.TomcatInfoUtils;
import cn.howardliu.monitor.cynomys.common.CommonParameters;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

import javax.servlet.ServletContext;

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
        CommonParameters.setServletContext(servletContext);
        try {
            if (TomcatInfoUtils.SERVER_IS_TOMCAT) {
                CommonParameters.setServerPort(TomcatInfoUtils.getPort());
            } else {
                String thePort = System.getProperty("server.port");
                if (thePort != null) {
                    CommonParameters.setServerPort(Integer.valueOf(thePort));
                }
            }
        } catch (Exception e) {
            logger.error("got server port exception", e);
        }
        if (logger.isInfoEnabled()) {
            logger.info("the server info: "
                    + "\n\t listen port = " + CommonParameters.getServerPort()
                    + "\n\t servlet info = " + CommonParameters.getServletContext().getServerInfo()
                    + "\n\t server major version = " + CommonParameters.getServletContext().getMajorVersion()
                    + "\n\t server minor version = " + CommonParameters.getServletContext().getMinorVersion()
                    + "\n\t server context name = " + CommonParameters.getServletContext().getServletContextName()
            );
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LaunchLatch.STARTED.start();
    }
}

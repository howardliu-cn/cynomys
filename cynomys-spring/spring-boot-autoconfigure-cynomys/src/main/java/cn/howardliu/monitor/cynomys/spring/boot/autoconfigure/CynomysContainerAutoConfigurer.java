package cn.howardliu.monitor.cynomys.spring.boot.autoconfigure;

import cn.howardliu.monitor.cynomys.common.CommonParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import javax.servlet.ServletContext;

/**
 * <br>created at 17-8-17
 *
 * @author liuxh
 * @since 0.0.1
 */
@Configuration
@ConditionalOnClass({CommonParameters.class, ServletContext.class})
public class CynomysContainerAutoConfigurer implements ApplicationListener<ServletWebServerInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CynomysContainerAutoConfigurer.class);

    @Override
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
        CommonParameters.setServerPort(event.getWebServer().getPort());
        CommonParameters.setServletContext(event.getApplicationContext().getServletContext());
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
}

package cn.howardliu.monitor.cynomys.spring.boot.autoconfigure;

import cn.howardliu.monitor.cynomys.common.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
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
@ConditionalOnClass({Constant.class, ServletContext.class})
public class CynomysContainerAutoConfigurer implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CynomysContainerAutoConfigurer.class);

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        Constant.serverPort = event.getEmbeddedServletContainer().getPort();
        Constant.servletContext = event.getApplicationContext().getServletContext();
        if (logger.isInfoEnabled()) {
            logger.info("the server info: "
                    + "\n\t listen port = " + Constant.serverPort
                    + "\n\t servlet info = " + Constant.servletContext.getServerInfo()
                    + "\n\t server major version = " + Constant.servletContext.getMajorVersion()
                    + "\n\t server minor version = " + Constant.servletContext.getMinorVersion()
                    + "\n\t server context name = " + Constant.servletContext.getServletContextName()
            );
        }
    }
}

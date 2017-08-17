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
public class CynomysAutoConfigurer implements ApplicationListener<EmbeddedServletContainerInitializedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CynomysAutoConfigurer.class);

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        Constant.SERVER_PORT = event.getEmbeddedServletContainer().getPort();
        Constant.SERVLET_CONTEXT = event.getApplicationContext().getServletContext();
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
}

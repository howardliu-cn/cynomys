package cn.howardliu.monitor.cynomys.spring.boot.autoconfigure;

import cn.howardliu.monitor.cynomys.common.CommonParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextStartedEvent;

import javax.sql.DataSource;
import java.util.Map;

/**
 * <br>created at 18-11-26
 *
 * @author liuxh
 * @since 1.0.0
 */
@Configuration
public class CynomysDatasourceAutoConfigurer2 implements ApplicationContextAware, ApplicationListener<ContextStartedEvent> {
    private static final Logger logger = LoggerFactory.getLogger(CynomysDatasourceAutoConfigurer2.class);
    private ApplicationContext applicationContext;

    @Override
    public void onApplicationEvent(ContextStartedEvent event) {
        Map<String, DataSource> dataSourceMap = this.applicationContext.getBeansOfType(DataSource.class);
        CommonParameters.getDataSources().putAll(dataSourceMap);
        if (logger.isInfoEnabled()) {
            final StringBuilder dsInfo = new StringBuilder();
            dsInfo.append("the datasource info (started): \n");
            for (Map.Entry<String, DataSource> entry : dataSourceMap.entrySet()) {
                dsInfo.append('\t').append(entry.getKey()).append(" = ").append(entry.getValue().getClass());
            }
            logger.info(dsInfo.toString());
        }

    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

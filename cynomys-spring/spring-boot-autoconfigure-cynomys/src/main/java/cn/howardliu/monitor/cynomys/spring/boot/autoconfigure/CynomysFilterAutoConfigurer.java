package cn.howardliu.monitor.cynomys.spring.boot.autoconfigure;

import cn.howardliu.monitor.cynomys.spring.boot.filter.CynomysFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * <br>created at 18-11-28
 *
 * @author liuxh
 * @since 1.0.0
 */
@Configuration
public class CynomysFilterAutoConfigurer {
    private static final Logger logger = LoggerFactory.getLogger(CynomysFilterAutoConfigurer.class);

    @Bean
    public CynomysFilter userFilter() {
        return new CynomysFilter();
    }

    @Bean
    public FilterRegistrationBean userFilterRegistration(CynomysFilter cynomysFilter) {
        FilterRegistrationBean<CynomysFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(cynomysFilter);
        registration.addUrlPatterns("/*");
        registration.setName("CynomysFilter");
        registration.setOrder(1);
        return registration;
    }
}

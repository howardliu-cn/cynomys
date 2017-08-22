package cn.howardliu.monitor.cynomys.spring.boot.autoconfigure;

import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
@Configuration
@ConditionalOnClass({LaunchLatch.class,})
public class CynomysContextRefreshedAutoConfigurer implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        LaunchLatch.LATCH.start();
    }
}

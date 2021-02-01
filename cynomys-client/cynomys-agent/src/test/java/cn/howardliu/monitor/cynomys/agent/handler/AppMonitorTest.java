package cn.howardliu.monitor.cynomys.agent.handler;

import org.junit.Test;

/**
 * <br>created at 17-8-17
 *
 * @author liuxh
 * @since 0.0.1
 */
public class AppMonitorTest {
    @Test
    public void buildAppInfo() throws Exception {
        System.out.println(AppMonitor.instance().buildAppInfo());
    }
}

package cn.howardliu.monitor.cynomys.common;

import org.junit.Test;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public class LaunchLatchTest {
    @Test
    public void test() throws Exception {
        LaunchLatch.CLIENT_INIT.start();
        System.out.println(1);
        LaunchLatch.STARTED.waitForMillis(2000);
        System.out.println(1);
    }
}

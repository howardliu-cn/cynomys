package cn.howardliu.monitor.cynomys.proxy.config;

import org.junit.Assert;
import org.junit.Test;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProxyConfigTest {
    @Test
    public void getPort() throws Exception {
        Assert.assertTrue(ProxyConfig.PROXY_CONFIG.getPort() > 0);
    }

    @Test
    public void getCport() throws Exception {
        Assert.assertEquals(ProxyConfig.PROXY_CONFIG.getPort(), 52700);
    }

}

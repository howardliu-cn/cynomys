package cn.howardliu.cynomys.warn;

import cn.howardliu.cynomys.warn.exception.ErrorLevel;
import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import org.junit.Test;

/**
 * <br>created at 17-8-29
 *
 * @author liuxh
 * @since 0.0.1
 */
public class WarnLoggingClientTest {
    @Test
    public void log() throws Exception {
        SystemPropertyConfig.init();
        WarnLoggingClient.INSTANCE.log("001", "e", "001", "e", ErrorLevel.WARNING, new RuntimeException());
    }
}
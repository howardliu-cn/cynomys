package cn.howardliu.monitor.cynomys.warn;

import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.warn.exception.ErrorLevel;
import org.junit.Ignore;
import org.junit.Test;

/**
 * <br>created at 17-8-29
 *
 * @author liuxh
 * @since 0.0.1
 */
@Ignore
public class WarnLoggingClientTest {
    @Test
    public void log() throws Exception {
        SystemPropertyConfig.init();
        WarnLoggingClient.INSTANCE.log("001", "e", "001", "e", ErrorLevel.WARNING, new RuntimeException());
    }
}

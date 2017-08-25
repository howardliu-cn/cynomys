package cn.howardliu.cynomys.warn;

import cn.howardliu.cynomys.warn.log.exception.ErrorLevel;
import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public class WarnLoggingClient implements Closeable {
    private static final Logger logger = LoggerFactory.getLogger(WarnLoggingClient.class);
    private final CynomysClient cynomysClient;

    public WarnLoggingClient() {
        // TODO not gracefully, must refactor
        if (Constant.NO_FLAG) {
            SystemPropertyConfig.init();
        } else {
            try {
                LaunchLatch.CLIENT_INIT.waitForMillis(120_000 + 2_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("LaunchLatch was interrupted!", e);
            }
        }
        this.cynomysClient = CynomysClientManager.INSTANCE
                .getAndCreateCynomysClient(
                        new ClientConfig(),
                        new SimpleChannelEventListener()
                );
    }

    public void log(String sysCode,
            String bizCode, String bizDesc,
            String errCode, String errDesc,
            String sysErrCode, String sysErrDesc,
            ErrorLevel errorLevel, String desc) {
        // TODO create ExceptionLog object and send to Cynomys Server
    }

    @Override
    public void close() throws IOException {
        if (this.cynomysClient != null) {
            this.cynomysClient.shutdown();
        }
    }
}

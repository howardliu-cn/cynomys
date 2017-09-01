package cn.howardliu.monitor.cynomys.agent.counter;

import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.JavaInformations;
import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class MonitorStarter {
    private static final Logger logger = LoggerFactory.getLogger(MonitorStarter.class);
    private static volatile boolean started = false;

    private MonitorStarter() {
    }

    public static synchronized void run() {
        if (!started) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    JavaInformations.instance(true);
                    Parameters.initialize();
                    SLACounter.init();
                    try {
                        if (!LaunchLatch.STARTED.waitForMillis(120_000)) {
                            logger.warn("Timeout(120000ms) when waiting for server started!");
                        }
                    } catch (InterruptedException e) {
                        logger.error("LaunchLatch was interrupted!", e);
                        Thread.currentThread().interrupt();
                    }
                    new MonitorChecker().startHealth("Active");
                }
            }, "monitor-starter-thread");
            thread.setDaemon(true);
            thread.start();

            started = true;
        }
    }
}

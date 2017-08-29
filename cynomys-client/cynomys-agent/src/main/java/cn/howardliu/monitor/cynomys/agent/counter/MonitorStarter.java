package cn.howardliu.monitor.cynomys.agent.counter;

import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.JavaInformations;
import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.client.common.Constant.started;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorStarter {
    private static final Logger logger = LoggerFactory.getLogger(MonitorStarter.class);

    public static synchronized void run() {
        if (!started) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    JavaInformations.instance(true);
                    Parameters.initialize();
                    SLACounter.init();
                    try {
                        LaunchLatch.STARTED.waitForMillis(120_000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        logger.error("LaunchLatch was interrupted!", e);
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

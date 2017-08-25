package cn.howardliu.monitor.cynomys.agent.counter;

import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.JavaInformations;
import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.client.common.Constant.started;
import static cn.howardliu.monitor.cynomys.common.Constant.SERVER_PORT;

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
            Thread thread = new Thread(() -> {
                JavaInformations javaInformations = JavaInformations.instance(true);
                Parameters.initialize();
                int port;
                if (javaInformations.getTomcatInformationsList().isEmpty()) {
                    port = Integer.valueOf(System.getProperty("server.port", SERVER_PORT + ""));
                } else {
                    port = Integer.valueOf(javaInformations.getTomcatInformationsList().get(0).getHttpPort());
                }
                SERVER_PORT = port;
                SLACounter.init();

                try {
                    LaunchLatch.LATCH.waitForMillis(120_000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    logger.error("LaunchLatch was interrupted!", e);
                }
                new MonitorChecker(port).startHealth("Active");
            });
            thread.setName("monitor-starter-thread");
            thread.setDaemon(true);
            thread.start();

            started = true;
        }
    }
}

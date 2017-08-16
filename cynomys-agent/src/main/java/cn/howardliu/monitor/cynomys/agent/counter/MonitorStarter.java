package cn.howardliu.monitor.cynomys.agent.counter;

import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.JavaInformations;
import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.started;
import static cn.howardliu.monitor.cynomys.common.Constant.SERVER_PORT;

/**
 * <br>created at 17-4-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorStarter {
    public static void run() {
        if (!started) {
            JavaInformations javaInformations = JavaInformations.instance(true);
            Parameters.initialize();
            int port;
            if (javaInformations.getTomcatInformationsList().isEmpty()) {
                // TODO read port use System.getProperty(String, String)
                port = Integer.valueOf(System.getProperty("server.port", SERVER_PORT + ""));
            } else {
                port = Integer.valueOf(javaInformations.getTomcatInformationsList().get(0).getHttpPort());
            }
            SERVER_PORT = port;
            SLACounter.init();
            new MonitorChecker(port).startHealth("Active");
            started = true;
        }
    }
}

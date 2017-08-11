package cn.howardliu.monitor.cynomys.agent.counter;

import cn.howardliu.monitor.cynomys.agent.conf.Constant;
import cn.howardliu.monitor.cynomys.agent.dto.JavaInformations;
import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;
import cn.howardliu.monitor.cynomys.agent.handler.factory.SLACountManager;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.RequestWrapper;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.*;

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
            Thread t = new Thread(new MonitorStarterRunner());
            t.setDaemon(true);
            t.setName("monitor-starter-thread");
            t.start();
            started = true;
        }
    }

    static class MonitorStarterRunner implements Runnable {
        @Override
        public void run() {
            if (IS_DEBUG) {
                return;
            }
            // TODO 暂时将监控代码放在下面，需要更优雅的写法
            JavaInformations javaInformations = JavaInformations.instance(SERVLET_CONTEXT, true);
            RequestWrapper.SINGLETON.initServletContext(SERVLET_CONTEXT);

            int port;
            if (javaInformations.getTomcatInformationsList().isEmpty()) {
                port = Integer.valueOf(System.getProperty(Constant.SYSTEM_SEETING_SERVER_DEFAULT_SERVER_PORT,
                        Constant.SYSTEM_SEETING_SERVER_DEFAULT_SERVER_PORT_VALUE));
            } else {
                port = Integer.valueOf(javaInformations.getTomcatInformationsList().get(0).getHttpPort());
            }

            SERVER_PORT = port;

            SLACountManager.init();

            // TODO read config server
            new MonitorChecker(port, SERVLET_CONTEXT).startHealth("Active");
        }
    }
}

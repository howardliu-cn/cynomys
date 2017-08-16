package cn.howardliu.monitor.cynomys.agent;

import cn.howardliu.monitor.cynomys.agent.conf.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.lang.instrument.Instrumentation;

import static cn.howardliu.monitor.cynomys.common.Constant.IS_DEBUG;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorAgent {
    public static void premain(String args, Instrumentation inst) {
        String agentFile = null;
        if (args != null && StringUtils.isNotBlank(args)) {
            File file = new File(args);
            if (file.exists() && !file.isDirectory() && file.isFile() && file.canRead()) {
                agentFile = args;
            }
        }

        SystemPropertyConfig.init(agentFile);

        if (IS_DEBUG) {
            System.err.println("the isDebug mode is " + IS_DEBUG);
            return;
        }

        SLACounter.init();
        inst.addTransformer(new MonitoringTransformer());
    }
}

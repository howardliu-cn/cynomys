package cn.howardliu.monitor.cynomys.agent;

import cn.howardliu.monitor.cynomys.agent.conf.EnvPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.conf.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;

import java.lang.instrument.Instrumentation;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.IS_DEBUG;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorAgent {
    public static void premain(String args, Instrumentation inst) {
        EnvPropertyConfig.init();
        if (args == null || args.isEmpty()) {
            SystemPropertyConfig.init();
        } else {
            SystemPropertyConfig.init(args);
        }
        if (IS_DEBUG) {
            return;
        }
        SLACounter.init();
        inst.addTransformer(new MonitoringTransformer());
    }
}

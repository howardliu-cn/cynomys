package cn.howardliu.monitor.cynomys.agent;

import cn.howardliu.monitor.cynomys.agent.counter.MonitorStarter;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.common.Constant;

import java.lang.instrument.Instrumentation;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorAgent {
    public static void premain(String args, Instrumentation inst) {
        SystemPropertyConfig.init(args);
        if (Constant.IS_DEBUG) {
            return;
        }
        inst.addTransformer(new MonitoringTransformer());
        Constant.NO_FLAG = false;
        MonitorStarter.run();
    }
}

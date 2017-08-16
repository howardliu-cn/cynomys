package cn.howardliu.monitor.cynomys.agent;

import cn.howardliu.monitor.cynomys.agent.conf.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Properties;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.IS_DEBUG;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MonitorAgent {
    private static final Logger logger = LoggerFactory.getLogger(MonitorAgent.class);

    public static void premain(String args, Instrumentation inst) {
        if (args != null && StringUtils.isNotBlank(args)) {
            try {
                FileInputStream fileInputStream = FileUtils.openInputStream(new File(args));
                Properties properties = new Properties();
                properties.load(fileInputStream);
                System.setProperties(properties);
            } catch (IOException e) {
                logger.error("read file {} exception", args, e);
                System.err.println("read file " + args + "exception, the cause is " + e);
            }
        }

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

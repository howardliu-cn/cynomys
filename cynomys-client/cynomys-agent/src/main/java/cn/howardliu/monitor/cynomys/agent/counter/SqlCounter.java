package cn.howardliu.monitor.cynomys.agent.counter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlCounter extends Counter {
    protected final String counterName = "sql";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected SqlCounter(String name) {
        super(name);
    }
}

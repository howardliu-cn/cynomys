package cn.howardliu.monitor.cynomys.agent.counter;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlCounter extends Counter {
    protected static final String counterName = "sql";

    protected SqlCounter(String name) {
        super(name);
    }
}

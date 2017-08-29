package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
public class StatementAspect {
    private static final Logger logger = LoggerFactory.getLogger(StatementAspect.class);
    private static final Map<Long, StatementExecuteWrapper> RUNNER_MAP = new ConcurrentHashMap<>();

    public static void begin(long tid, String sql) {
        if (RUNNER_MAP.containsKey(tid)) {
            return;
        } else {
            RUNNER_MAP.put(tid, new StatementExecuteWrapper(tid));
        }
        JdbcWrapper.ACTIVE_CONNECTION_COUNT.incrementAndGet();
        JdbcWrapper.SINGLETON.getSqlCounter().bindContext(sql, sql, null, -1);
    }

    public static void catchBlock(long tid, Throwable cause) {
        assert cause != null;
        StatementExecuteWrapper wrapper = RUNNER_MAP.get(tid);
        if (wrapper == null) {
            return;
        }
        wrapper.setCause(cause);
    }


    public static void end(long tid, String methodName, String sql) {
        StatementExecuteWrapper wrapper = RUNNER_MAP.remove(tid);
        if (wrapper == null) {
            return;
        }
        long duration = System.currentTimeMillis() - wrapper.getStartTime();
        JdbcWrapper.ACTIVE_CONNECTION_COUNT.decrementAndGet();
        //noinspection ThrowableResultOfMethodCallIgnored
        Throwable cause = wrapper.getCause();
        JdbcWrapper.SINGLETON.getSqlCounter().addRequest(sql, duration, -1, cause != null, -1);
        if (logger.isDebugEnabled()) {
            logger.debug("{} used {}ms, sql is [{}]", methodName, duration, sql);
            if (cause != null) {
                logger.debug("sql [{}] run error", sql, cause);
            }
        }
    }

    static class StatementExecuteWrapper extends RunnerWrapper {
        StatementExecuteWrapper(long tid) {
            super(tid);
        }
    }
}

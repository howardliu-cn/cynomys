package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.common.SqlHolder;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class PreparedStatementAspect {
    private static final Logger logger = LoggerFactory.getLogger(PreparedStatementAspect.class);
    private static final Map<Long, ExecuteRunnerWrapper> RUNNER_MAP =
            Collections.synchronizedMap(new HashMap<Long, ExecuteRunnerWrapper>());

    public static void begin(long tid, PreparedStatement stmt) {
        assert stmt != null;
        int identityHashCode = System.identityHashCode(stmt);
        if (!SqlHolder.contains(identityHashCode)) {
            return;
        }
        if (RUNNER_MAP.containsKey(tid)) {
            return;
        } else {
            RUNNER_MAP.put(tid, new ExecuteRunnerWrapper(tid));
        }
        JdbcWrapper.ACTIVE_CONNECTION_COUNT.incrementAndGet();
        String sql = SqlHolder.get(identityHashCode);
        JdbcWrapper.SINGLETON.getSqlCounter().bindContext(sql, sql, null, -1);
    }

    public static void catchBlock(long tid, Throwable cause) {
        assert cause != null;
        ExecuteRunnerWrapper wrapper = RUNNER_MAP.get(tid);
        if (wrapper == null) {
            return;
        }
        wrapper.setCause(cause);
    }

    public static void end(long tid, String methodName, PreparedStatement stmt) {
        ExecuteRunnerWrapper wrapper = RUNNER_MAP.remove(tid);
        if (wrapper == null) {
            return;
        }
        int identityHashCode = System.identityHashCode(stmt);
        String sql = SqlHolder.get(identityHashCode);
        if (sql == null) {
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

    public static void close(Object stmt) {
        SqlHolder.remove(System.identityHashCode(stmt));
    }

    static class ExecuteRunnerWrapper extends RunnerWrapper {
        ExecuteRunnerWrapper(long tid) {
            super(tid);
        }
    }
}

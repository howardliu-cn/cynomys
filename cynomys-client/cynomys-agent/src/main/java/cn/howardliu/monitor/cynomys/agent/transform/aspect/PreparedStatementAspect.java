package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.cache.SqlCacheHolder;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class PreparedStatementAspect {
    private static final Logger logger = LoggerFactory.getLogger(PreparedStatementAspect.class);
    private static final Map<Long, ExecuteRunnerWrapper> RUNNER_MAP = new ConcurrentHashMap<>();

    private PreparedStatementAspect() {
    }

    public static void begin(long tid, PreparedStatement stmt) {
        assert stmt != null;
        int identityHashCode = System.identityHashCode(stmt);
        if (!SqlCacheHolder.SQL_HOLDER.contains(identityHashCode)) {
            logger.trace("identityHashCode {} not in sql_holder", identityHashCode);
            return;
        }
        if (RUNNER_MAP.containsKey(tid)) {
            logger.trace("tid {} is already in runner map", tid);
            return;
        } else {
            RUNNER_MAP.put(tid, new ExecuteRunnerWrapper(tid));
        }
        JdbcWrapper.ACTIVE_CONNECTION_COUNT.incrementAndGet();
        String sql = SqlCacheHolder.SQL_HOLDER.get(identityHashCode);
        logger.trace("run sql: {}", sql);
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
        final ExecuteRunnerWrapper wrapper = RUNNER_MAP.get(tid);
        if (wrapper == null) {
            return;
        } else {
            RUNNER_MAP.remove(tid);
        }
        int identityHashCode = System.identityHashCode(stmt);
        String sql = SqlCacheHolder.SQL_HOLDER.get(identityHashCode);
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
        JdbcWrapper.SINGLETON.getSqlCounter().unbindContext();
    }

    public static void close(Object stmt) {
        SqlCacheHolder.SQL_HOLDER.remove(System.identityHashCode(stmt));
    }

    static class ExecuteRunnerWrapper extends RunnerWrapper {
        ExecuteRunnerWrapper(long tid) {
            super(tid);
        }
    }
}


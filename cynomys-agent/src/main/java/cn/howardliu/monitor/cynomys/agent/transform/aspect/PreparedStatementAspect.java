package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.common.SqlHolder;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import cn.howardliu.monitor.cynomys.common.ThreadMXBeanUtils;
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
        long startTime = System.currentTimeMillis();
        long startThreadCupTime = ThreadMXBeanUtils.getThreadCpuTime(tid);
        ExecuteRunnerWrapper wrapper = new ExecuteRunnerWrapper();
        wrapper.setStartTime(startTime);
        wrapper.setStartThreadCupTime(startThreadCupTime);
        wrapper.setHashCode(identityHashCode);
        RUNNER_MAP.put(tid, wrapper);
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

    public static void end(long tid, String methodName) {
        ExecuteRunnerWrapper wrapper = RUNNER_MAP.remove(tid);
        if (wrapper == null) {
            return;
        }
        String sql = SqlHolder.get(wrapper.getHashCode());
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
            if(cause != null) {
                logger.debug("sql [{}] run error", sql, cause);
            }
        }
    }

    public static void close(Object stmt) {
        SqlHolder.remove(System.identityHashCode(stmt));
    }

    static class ExecuteRunnerWrapper {
        private long tid;
        private int hashCode;
        private long startTime;
        private long startThreadCupTime;
        private Throwable cause;

        public long getTid() {
            return tid;
        }

        public void setTid(long tid) {
            this.tid = tid;
        }

        public int getHashCode() {
            return hashCode;
        }

        public void setHashCode(int hashCode) {
            this.hashCode = hashCode;
        }

        public long getStartTime() {
            return startTime;
        }

        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }

        public long getStartThreadCupTime() {
            return startThreadCupTime;
        }

        public void setStartThreadCupTime(long startThreadCupTime) {
            this.startThreadCupTime = startThreadCupTime;
        }

        public Throwable getCause() {
            return cause;
        }

        public void setCause(Throwable cause) {
            this.cause = cause;
        }
    }
}

package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

import static cn.howardliu.monitor.cynomys.agent.cache.SqlCacheHolder.SQL_HOLDER;
import static cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper.TRANSACTION_COUNT;
import static cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper.USED_CONNECTION_COUNT;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public final class ConnectionAspect {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionAspect.class);

    private ConnectionAspect() {
    }

    public static void constructorEnd(Connection connection) {
        if (JdbcWrapper.SINGLETON.addConnectionInformation(connection)) {
            USED_CONNECTION_COUNT.incrementAndGet();
            TRANSACTION_COUNT.incrementAndGet();
        }
    }

    public static void catchStatementAndSql(String sql, Object stmt) {
        assert sql != null;
        assert stmt != null;
        final int identityHashCode = System.identityHashCode(stmt);
        logger.trace("run sql {} in {}", sql, identityHashCode);
        if (!JdbcWrapper.SINGLETON.getSqlCounter().isDisplayed() || sql.contains("explain ")) {
            return;
        }
        logger.trace("put identityHashCode {} into sql_holder", identityHashCode);
        SQL_HOLDER.add(identityHashCode, sql);
    }
}

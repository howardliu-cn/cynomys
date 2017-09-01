package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.common.SqlHolder;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;

import java.sql.Connection;

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
        if (!JdbcWrapper.SINGLETON.getSqlCounter().isDisplayed() || sql.contains("explain ")) {
            return;
        }
        SqlHolder.add(System.identityHashCode(stmt), sql);
    }
}

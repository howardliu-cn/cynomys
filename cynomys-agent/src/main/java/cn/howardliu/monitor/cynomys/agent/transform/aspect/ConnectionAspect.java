package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.common.SqlHolder;
import cn.howardliu.monitor.cynomys.agent.dto.ConnectionInformations;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;

import java.sql.Connection;

import static cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper.*;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
@SuppressWarnings("unused")
public class ConnectionAspect {
    public static void constructorEnd(Connection connection) {
        assert connection != null;
        synchronized (USED_CONNECTION_INFORMATIONS) {
            int uniqueIdOfConnection = System.identityHashCode(connection);
            if (USED_CONNECTION_INFORMATIONS.containsKey(uniqueIdOfConnection)) {
                return;
            }

            if (SINGLETON.isConnectionInformationsEnabled()
                    && USED_CONNECTION_INFORMATIONS.size() < MAX_USED_CONNECTION_INFORMATIONS) {
                USED_CONNECTION_INFORMATIONS.put(uniqueIdOfConnection, new ConnectionInformations());
            }
        }

        USED_CONNECTION_COUNT.incrementAndGet();
        TRANSACTION_COUNT.incrementAndGet();
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

package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.common.SqlHolder;
import cn.howardliu.monitor.cynomys.agent.dto.ConnectionInformations;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper.*;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConnectionAspect {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final Set<String> hashCodeSet = Collections.synchronizedSet(new HashSet<String>());

    public static void constructorEnd(Connection connection) {
        assert connection != null;
        synchronized (USED_CONNECTION_INFORMATIONS) {
            int uniqueIdOfConnection = ConnectionInformations.getUniqueIdOfConnection(connection);
            if (USED_CONNECTION_INFORMATIONS.containsKey(
                    uniqueIdOfConnection)) {
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

package cn.howardliu.cynomys.warn.log.exception;

import cn.howardliu.cynomys.warn.log.WarnLogCaching;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteException;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import javax.sql.ConnectionEvent;
import javax.sql.ConnectionEventListener;
import javax.sql.PooledConnection;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * <br>created at 17-8-28
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum ExceptionLogCaching implements WarnLogCaching<ExceptionLog> {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(ExceptionLogCaching.class);
    private static final SQLiteConnectionPoolDataSource dataSource;
    private static PooledConnection pooledConnection;

    static {
        dataSource = new SQLiteConnectionPoolDataSource();
        dataSource.setDatabaseName("exceptionLog");
        dataSource.setUrl("jdbc:sqlite:exceptionLog.db");

        try {
            pooledConnection = dataSource.getPooledConnection();
            pooledConnection.addConnectionEventListener(new ConnectionEventListener() {
                @Override
                public void connectionClosed(ConnectionEvent event) {
                    try {
                        pooledConnection = dataSource.getPooledConnection();
                    } catch (SQLException e) {
                        logger.error("get PooledConnection exception", e);
                    }
                }

                @Override
                public void connectionErrorOccurred(ConnectionEvent event) {
                    try {
                        pooledConnection.close();
                        pooledConnection = dataSource.getPooledConnection();
                    } catch (SQLException e) {
                        logger.error("close and get PooledConnection exception", e);
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ExceptionLog> list(int from, int rows) throws SQLException {
        List<ExceptionLog> result = new ArrayList<>(rows);
        try (Connection conn = pooledConnection.getConnection()) {
            tableExist(conn);
            try (PreparedStatement stmt = conn
                    .prepareStatement("SELECT exception_msg FROM exception_log WHERE status=1 LIMIT ? OFFSET ?")) {
                stmt.setInt(1, rows);
                stmt.setInt(2, from);
                try (ResultSet rs = stmt.executeQuery()) {
                    result.add(JSON.parseObject(rs.getString("exception_msg"), ExceptionLog.class));
                }
            }
        }
        return result;
    }

    @Override
    public void upsert(String errId, String exceptionMsg) throws SQLException {
        try (Connection conn = pooledConnection.getConnection()) {
            tableExist(conn);
            try (PreparedStatement stmt = conn
                    .prepareStatement("INSERT INTO exception_log(err_id, exception_msg, status) VALUES (?, ?, ?)")) {
                stmt.setString(1, errId);
                stmt.setString(2, exceptionMsg);
                stmt.setInt(3, 1);
                stmt.execute();
            } catch (SQLiteException e) {
                if (e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
                    // ignored
                } else {
                    throw e;
                }
            }
        }
    }

    @Override
    public void delete(String errId) throws SQLException {
        try (Connection conn = pooledConnection.getConnection()) {
            tableExist(conn);
            try (PreparedStatement stmt = conn
                    .prepareStatement("DELETE FROM exception_log WHERE err_id=?")) {
                stmt.setString(1, errId);
                stmt.execute();
            }
        }
    }

    private void tableExist(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS exception_log (" +
                    "err_id CHAR(50) PRIMARY KEY, " +
                    "exception_msg TEXT NOT NULL, " +
                    "status INT DEFAULT 0" +
                    ")");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            if (pooledConnection != null) {
                pooledConnection.close();
            }
        } catch (SQLException e) {
            logger.error("close PooledConnection exception", e);
        }
    }
}

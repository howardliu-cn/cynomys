//package cn.howardliu.monitor.cynomys.agent.cache;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.sqlite.javax.SQLiteConnectionPoolDataSource;
//
//import javax.sql.ConnectionEvent;
//import javax.sql.ConnectionEventListener;
//import javax.sql.PooledConnection;
//import java.io.IOException;
//import java.sql.*;
//
///**
// * <br>created at 2019-05-29
// *
// * @author liuxh
// * @since 1.0.0
// */
//public enum SqlDbHolder implements SqlHolder {
//    SQL_HOLDER;
//
//    private static final Logger logger = LoggerFactory.getLogger(SqlDbHolder.class);
//    private static final SQLiteConnectionPoolDataSource dataSource;
//    private static PooledConnection pooledConnection;
//
//    static {
//        dataSource = new SQLiteConnectionPoolDataSource();
//        dataSource.setDatabaseName("monitor_sql_info");
//        dataSource.setUrl("jdbc:sqlite:monitor_sql_info.db");
//        init();
//    }
//
//    private static void init() {
//        try {
//            pooledConnection = dataSource.getPooledConnection();
//            pooledConnection.addConnectionEventListener(new ConnectionEventListener() {
//                @Override
//                public void connectionClosed(ConnectionEvent event) {
//                    try {
//                        pooledConnection = dataSource.getPooledConnection();
//                    } catch (SQLException e) {
//                        logger.error("get PooledConnection exception", e);
//                    }
//                }
//
//                @Override
//                public void connectionErrorOccurred(ConnectionEvent event) {
//                    try {
//                        pooledConnection.close();
//                        pooledConnection = dataSource.getPooledConnection();
//                    } catch (SQLException e) {
//                        logger.error("close and get PooledConnection exception", e);
//                    }
//                }
//            });
//        } catch (Exception e) {
//            logger.error("get pooledConnection exception", e);
//        }
//    }
//
//    @Override
//    public boolean contains(final int hashCode) {
//        try {
//            Connection conn = getConnection();
//            tableExist(conn);
//            try (PreparedStatement stmt = conn.prepareStatement("SELECT 1 FROM sql_info WHERE hash_code=?")) {
//                stmt.setInt(1, hashCode);
//                try (ResultSet rs = stmt.executeQuery()) {
//                    return rs.next();
//                }
//            }
//        } catch (Exception e) {
//            if (e.getMessage().contains("Connection is closed")) {
//                init();
//            }
//            return false;
//        }
//    }
//
//    @Override
//    public synchronized String add(final int hashCode, final String sql) {
//        try {
//            Connection conn = getConnection();
//            tableExist(conn);
//            try (PreparedStatement stmt = conn.prepareStatement("INSERT INTO sql_info(hash_code, sql_content) VALUES (?, ?)")) {
//                stmt.setInt(1, hashCode);
//                stmt.setString(2, sql);
//                stmt.execute();
//            } catch (Exception e) {
//                if (e.getMessage().contains("Connection is closed")) {
//                    init();
//                } else if (!e.getMessage().contains("SQLITE_CONSTRAINT_PRIMARYKEY")) {
//                    throw e;
//                }
//            }
//        } catch (Exception e) {
//            logger.error("SQL信息添加失败：[hashCode={}, sql={}]", hashCode, sql, e);
//        }
//        return null;
//    }
//
//    @Override
//    public String get(final int hashCode) {
//        try {
//            Connection conn = getConnection();
//            tableExist(conn);
//            try (PreparedStatement stmt = conn.prepareStatement("SELECT sql_content FROM sql_info WHERE hash_code=?")) {
//                stmt.setInt(1, hashCode);
//                try (ResultSet rs = stmt.executeQuery()) {
//                    if (rs.next()) {
//                        return rs.getString("sql_content");
//                    }
//                }
//            }
//        } catch (Exception e) {
//            if (e.getMessage().contains("Connection is closed")) {
//                init();
//            } else {
//                logger.error("SQL信息查询失败：[hashCode={}]", hashCode, e);
//            }
//        }
//        return null;
//    }
//
//    @Override
//    public synchronized String remove(final int hashCode) {
//        try {
//            Connection conn = getConnection();
//            tableExist(conn);
//            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM sql_info WHERE hash_code=?")) {
//                stmt.setInt(1, hashCode);
//                stmt.execute();
//            }
//        } catch (Exception e) {
//            if (e.getMessage().contains("Connection is closed")) {
//                init();
//            } else {
//                logger.error("SQL信息删除失败：[hashCode={}]", hashCode, e);
//            }
//        }
//        return null;
//    }
//
//    private synchronized void tableExist(Connection conn) throws SQLException {
//        Statement stmt = conn.createStatement();
//        stmt.executeUpdate("CREATE TABLE IF NOT EXISTS sql_info (" +
//                " hash_code int PRIMARY KEY, " +
//                " sql_content TEXT NOT NULL " +
//                " )");
//    }
//
//    private synchronized Connection getConnection() throws SQLException {
//        return pooledConnection.getConnection();
//    }
//
//    public void close() throws IOException {
//        try {
//            if (pooledConnection != null) {
//                pooledConnection.close();
//            }
//        } catch (SQLException e) {
//            logger.error("close PooledConnection exception", e);
//        }
//    }
//}

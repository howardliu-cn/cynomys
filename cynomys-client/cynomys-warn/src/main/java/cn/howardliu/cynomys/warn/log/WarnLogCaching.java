package cn.howardliu.cynomys.warn.log;

import java.io.Closeable;
import java.sql.SQLException;
import java.util.List;

/**
 * <br>created at 17-8-28
 *
 * @author liuxh
 * @since 0.0.1
 */
public interface WarnLogCaching<T extends WarnLog> extends Closeable {
    List<T> list(int from, int rows) throws SQLException;

    void upsert(String id, String warnLog) throws SQLException;

    void delete(String id) throws SQLException;
}

package cn.howardliu.monitor.cynomys.agent.common;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class SqlHolder {
    private static final Map<String, String> STATEMENT_HASHCODE_SQL_MAP = new ConcurrentHashMap<>();

    private SqlHolder() {
    }

    public static boolean contains(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.containsKey(Integer.toString(hashCode));
    }

    public static String add(int hashCode, String sql) {
        return STATEMENT_HASHCODE_SQL_MAP.put(Integer.toString(hashCode), sql);
    }

    public static String get(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.get(Integer.toString(hashCode));
    }

    public static String remove(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.remove(Integer.toString(hashCode));
    }
}

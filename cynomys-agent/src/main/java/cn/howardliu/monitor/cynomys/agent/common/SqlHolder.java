package cn.howardliu.monitor.cynomys.agent.common;

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
public class SqlHolder {
    private static final Map<String, String> STATEMENT_HASHCODE_SQL_MAP =
            Collections.synchronizedMap(new HashMap<String, String>());

    public static boolean contains(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.containsKey(hashCode + "");
    }

    public static String add(int hashCode, String sql) {
        return STATEMENT_HASHCODE_SQL_MAP.put(hashCode + "", sql);
    }

    public static String get(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.get(hashCode + "");
    }

    public static String remove(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.remove(hashCode + "");
    }
}

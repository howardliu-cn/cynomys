package cn.howardliu.monitor.cynomys.agent.common;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>created at 17-4-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Printer {
    public static final Map<String, String> STATEMENT_HASHCODE_SQL_MAP = Collections
            .synchronizedMap(new HashMap<String, String>());

    public static String remove(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.remove(hashCode + "");
    }

    public static String add(int hashCode, String sql) {
        return STATEMENT_HASHCODE_SQL_MAP.put(hashCode + "", sql);
    }

    public static String get(int hashCode) {
        return STATEMENT_HASHCODE_SQL_MAP.get(hashCode + "");
    }

    public static void printBeforeRequest(HttpServletRequest request) {
        System.err.println(request.getRequestURI());
    }

    public static void printAfterCreatePreparedStatement(Object connection, String sql, Object stmt) {
        System.err.println("this object's hashcode is " + System.identityHashCode(connection));
        System.err.println("the sql is: [" + sql + "]");
        System.err.println("the result is: [" + stmt + "]");
        int resultHashCode = System.identityHashCode(stmt);
        System.err.println("the result's hashcode is: [" + resultHashCode + "]");
        Printer.add(resultHashCode, sql);
        System.out.println(STATEMENT_HASHCODE_SQL_MAP);
    }

    public static void printAfterExecuteInPreparedStatement(Object stmt) {
        int stmtHashCode = System.identityHashCode(stmt);
        System.err.println("this object's hashcode is " + stmtHashCode);
        System.err.println("the sql is: [" + Printer.get(stmtHashCode) + "]");
        System.out.println(STATEMENT_HASHCODE_SQL_MAP);
    }

    public static void printAfterPreparedStatementClose(Object stmt) {
        Printer.remove(System.identityHashCode(stmt));
        System.out.println(STATEMENT_HASHCODE_SQL_MAP);
    }
}

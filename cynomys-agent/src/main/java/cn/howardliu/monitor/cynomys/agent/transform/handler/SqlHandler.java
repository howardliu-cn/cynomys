package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlHandler extends MethodRewriteHandler {
    private static final SqlHandler _HANDLER = new SqlHandler();
    private static volatile boolean isInit = false;

    public static synchronized SqlHandler instance() {
        if (!isInit) {
            _HANDLER
                    .addLast(DataSourceHandler.instance())
                    .addLast(new ConnectionHandler())
                    .addLast(new PreparedStatementHandler())
                    .addLast(new StatementHandler());
            isInit = true;
        }
        return _HANDLER;
    }
}

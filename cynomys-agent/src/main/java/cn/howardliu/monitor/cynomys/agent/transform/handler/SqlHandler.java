package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SqlHandler extends MethodRewriteHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final SqlHandler _HANDLER = new SqlHandler();

    static {
        _HANDLER
                .addLast(DataSourceHandler.instance())
                .addLast(new ConnectionHandler())
                .addLast(new PreparedStatementHandler())
                .addLast(new StatementHandler());
    }

    public static SqlHandler instance() {
        return _HANDLER;
    }
}

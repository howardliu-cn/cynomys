package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import javassist.CtClass;

import javax.sql.DataSource;

/**
 * <br>created at 17-4-13
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataSourceHandler extends SqlHandler {
    private static final DataSourceHandler _HANDLER = new DataSourceHandler();
    private static volatile boolean isInit = false;

    public static synchronized DataSourceHandler instance() {
        if (!isInit) {
            _HANDLER.addLast(new DruidDataSourceHandler());
            isInit = true;
        }
        return _HANDLER;
    }

    boolean isDataSource(CtClass ctClass) {
        return MethodRewriteHandler.isImpl(ctClass, DataSource.class);
    }
}

package cn.howardliu.monitor.cynomys.agent.transform.handler;

import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * <br>created at 17-4-13
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class DataSourceHandler extends SqlHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final DataSourceHandler _HANDLER = new DataSourceHandler();

    static {
        _HANDLER.addLast(new DruidDataSourceHandler());
    }

    public static DataSourceHandler instance() {
        return _HANDLER;
    }

    protected boolean isDataSource(CtClass ctClass) {
        return isImpl(ctClass, DataSource.class);
    }
}

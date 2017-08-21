package cn.howardliu.monitor.cynomys.agent.transform.handler;

import javassist.CtClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class StatementHandler extends SqlHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doWeave(CtClass ctClass) {
        if (isStatement(ctClass)) {
            logger.info("begin to wrap Statement");
            // TODO weave Statement's method
//        execute
//        executeQuery
//        executeUpdate
//        executeBatch
//        executeLargeBatch
//        executeLargeUpdate
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private boolean isStatement(CtClass ctClass) {
        return isImpl(ctClass, "java.sql.Statement") || isChild(ctClass, "java.sql.Statement");
    }
}

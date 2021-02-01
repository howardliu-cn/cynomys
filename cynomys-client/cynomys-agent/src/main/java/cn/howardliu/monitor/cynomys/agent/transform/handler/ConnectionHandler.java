package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConnectionHandler extends SqlHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doWeave(CtClass ctClass) {
        if (isConnection(ctClass)) {
            logger.info("begin to wrap Connection");
            CtConstructor[] constructors = ctClass.getDeclaredConstructors();
            for (CtConstructor constructor : constructors) {
                try {
                    constructor.insertAfter(
                            "cn.howardliu.monitor.cynomys.agent.transform.aspect.ConnectionAspect.constructorEnd($0);");
                } catch (CannotCompileException e) {
                    logger.error("cannot compile exception: ConnectionAspect", e);
                }
            }
            prepareMethodWeave(ctClass, "prepareStatement");
            prepareMethodWeave(ctClass, "prepareCall");
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private void prepareMethodWeave(CtClass ctClass, String methodName) {
        try {
            CtMethod[] ctMethods = ctClass.getDeclaredMethods(methodName);
            for (CtMethod ctMethod : ctMethods) {
                ctMethod.insertAfter(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.ConnectionAspect.catchStatementAndSql($1, $_);");
            }
        } catch (Throwable t) {
            logger.warn("SKIPPED " + methodName + " in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private boolean isConnection(CtClass ctClass) {
        return MethodRewriteHandler.isImpl(ctClass, Connection.class)
                ||
                MethodRewriteHandler.isChild(ctClass, Connection.class);
    }
}

package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class PreparedStatementHandler extends SqlHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ClassPool classPool = MonitoringTransformer.getClassPool();

    @Override
    public void doWeave(CtClass ctClass) {
        if (isPreparedStatement(ctClass)) {
            logger.info("begin to wrap PreparedStatement");
            doWeave(ctClass, "execute");
            doWeave(ctClass, "executeQuery");
            doWeave(ctClass, "executeUpdate");
            doWeave(ctClass, "executeLargeUpdate");
            doWeaveClose(ctClass);
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private void doWeave(CtClass ctClass, String methodName) {
        try {
            CtMethod[] ctMethods = ctClass.getDeclaredMethods(methodName);
            for (CtMethod ctMethod : ctMethods) {
                ctMethod.insertBefore(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.PreparedStatementAspect.begin(Thread.currentThread().getId(), $0);"
                );
                ctMethod.addCatch(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.PreparedStatementAspect.catchBlock(Thread.currentThread().getId(), $e); throw $e;",
                        classPool.get("java.lang.Throwable")
                );
                ctMethod.insertAfter(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.PreparedStatementAspect.end(Thread.currentThread().getId(), \"" + ctMethod
                                .getLongName() + "\", $0);",
                        true
                );
            }
        } catch (Throwable t) {
            logger.warn("SKIPPED " + methodName + " in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private void doWeaveClose(CtClass ctClass) {
        try {
            CtMethod[] ctMethods = ctClass.getDeclaredMethods("close");
            for (CtMethod ctMethod : ctMethods) {
                ctMethod.insertAfter("cn.howardliu.monitor.cynomys.agent.transform.aspect.PreparedStatementAspect.close($0);");
            }
        } catch (Throwable t) {
            logger.warn("SKIPPED close() in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private boolean isPreparedStatement(CtClass ctClass) {
        return MethodRewriteHandler.isImpl(ctClass, "java.sql.PreparedStatement") || MethodRewriteHandler.isChild(ctClass, "java.sql.PreparedStatement");
    }
}

package cn.howardliu.monitor.cynomys.agent.transform.handler;

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
public class StatementHandler extends SqlHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ClassPool classPool = ClassPool.getDefault();

    @Override
    public void doWeave(CtClass ctClass) {
        if (isStatement(ctClass)) {
            logger.info("begin to wrap Statement");
            // TODO weave Statement's method
//        executeBatch
//        executeLargeBatch
            doWeave(ctClass, "execute");
            doWeave(ctClass, "executeQuery");
            doWeave(ctClass, "executeUpdate");
            doWeave(ctClass, "executeLargeUpdate");
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private void doWeave(CtClass ctClass, String methodName) {
        String ctClassName = ctClass.getName();
        try {
            CtMethod[] ctMethods = ctClass.getDeclaredMethods(methodName);
            for (CtMethod ctMethod : ctMethods) {
                CtClass[] parameterTypes = ctMethod.getParameterTypes();
                if (parameterTypes.length == 0 || !parameterTypes[0].getName().equals("java.lang.String")) {
                    if (!"org.sqlite.jdbc3.JDBC3PreparedStatement".equals(ctClassName)) {
                        StringBuilder errMsg = new StringBuilder(ctClassName + "#" + methodName + "(");
                        for (CtClass parameterType : parameterTypes) {
                            errMsg.append(parameterType.getName()).append(", ");
                        }
                        errMsg.append(")");
                        System.err.println("\n\n===========================================================");
                        System.err.println("If you saw this line, email howardliu1988@163.com, please!" +
                                " Or your application will be destroy by some unknown error!");
                        System.err.println(errMsg);
                        System.err.println("\n\n===========================================================");
                    }
                    return;
                }
                ctMethod.insertBefore(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.StatementAspect.begin(Thread.currentThread().getId(), $1);"
                );
                ctMethod.addCatch(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.StatementAspect.catchBlock(Thread.currentThread().getId(), $e); throw $e;",
                        classPool.get("java.lang.Throwable")
                );
                ctMethod.insertAfter(
                        "cn.howardliu.monitor.cynomys.agent.transform.aspect.StatementAspect.end(Thread.currentThread().getId(), \"" + ctMethod
                                .getLongName() + "\", $1);",
                        true
                );
            }
        } catch (Exception e) {
            logger.warn("SKIPPED " + methodName + " in " + ctClassName + ", the reason is " + e.getMessage());
        }
    }

    private boolean isStatement(CtClass ctClass) {
        return isImpl(ctClass, "java.sql.Statement") || isChild(ctClass, "java.sql.Statement");
    }
}

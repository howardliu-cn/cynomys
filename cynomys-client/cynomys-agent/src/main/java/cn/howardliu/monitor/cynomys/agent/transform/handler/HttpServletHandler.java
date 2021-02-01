package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class HttpServletHandler extends MethodRewriteHandler {
    private static final HttpServletHandler SERVLET_HANDLER = new HttpServletHandler();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private ClassPool classPool = MonitoringTransformer.getClassPool();

    public static HttpServletHandler instance() {
        return SERVLET_HANDLER;
    }

    public void doWeave(CtClass ctClass) {
        if (isHttpServlet(ctClass)) {
            logger.debug("begin to wrapping HttpServlet");
            doWeaveInit(ctClass);
            doWeave(ctClass, "doHead");
            doWeave(ctClass, "doGet");
            doWeave(ctClass, "doPost");
            doWeave(ctClass, "doPut");
            doWeave(ctClass, "doDelete");
            doWeave(ctClass, "doOptions");
            doWeave(ctClass, "doTrace");
            logger.debug("ended wrap HttpServlet");
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private boolean isHttpServlet(CtClass ctClass) {
        return isChild(ctClass, HttpServlet.class)
                ||
                isChild(ctClass, "org.springframework.web.servlet.HttpServletBean")
                ;
    }

    private void doWeaveInit(CtClass ctClass) {
        try {
            CtMethod ctMethod = ctClass.getDeclaredMethod("init");
            ctMethod.insertAfter(
                    "if(cn.howardliu.monitor.cynomys.common.CommonParameters.servletContext == null) {" +
                            "cn.howardliu.monitor.cynomys.common.CommonParameters.servletContext = $0.getServletContext();" +
                            "}"
            );
        } catch (NotFoundException e) {
            logger.trace("not found init method in " + ctClass.getName());
        } catch (Throwable t) {
            logger.warn("SKIPPED init() in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private void doWeave(CtClass ctClass, String methodName) {
        try {
            CtClass[] params = {
                    classPool.get(HttpServletRequest.class.getName()),
                    classPool.get(HttpServletResponse.class.getName())
            };
            CtMethod ctMethod = ctClass.getDeclaredMethod(methodName, params);
            ctMethod.insertBefore(
                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.begin(Thread.currentThread().getId(), $1, $2);"
            );
            ctMethod.addCatch(
                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.catchBlock(Thread.currentThread().getId(), $1, $2, $e);" +
                            "throw $e;",
                    classPool.get("java.lang.Throwable")
            );
            ctMethod.insertAfter(
                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.end(Thread.currentThread().getId(), $1, $2);"
            );
        } catch (NotFoundException e) {
            logger.trace("not found " + methodName + " method in " + ctClass.getName());
        } catch (Throwable t) {
            logger.warn("SKIPPED " + methodName + " in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }
}

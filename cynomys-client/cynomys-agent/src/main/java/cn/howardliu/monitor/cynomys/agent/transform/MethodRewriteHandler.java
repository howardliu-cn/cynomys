package cn.howardliu.monitor.cynomys.agent.transform;

import cn.howardliu.monitor.cynomys.agent.transform.handler.HttpServletHandler;
import cn.howardliu.monitor.cynomys.agent.transform.handler.SqlHandler;
import javassist.CtClass;
import javassist.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MethodRewriteHandler {
    private static final Logger logger = LoggerFactory.getLogger(MethodRewriteHandler.class);
    private static final MethodRewriteHandler _HANDLER = new MethodRewriteHandler();
    private static volatile boolean isInit = false;
    protected MethodRewriteHandler handler = null;

    public static synchronized MethodRewriteHandler instance() {
        if (!isInit) {
            _HANDLER.addLast(HttpServletHandler.instance())
                    .addLast(SqlHandler.instance())
            ;
            isInit = true;
        }
        return _HANDLER;
    }

    protected static boolean isChild(CtClass ctClass, Class<?> clazz) {
        return isChild(ctClass, clazz.getName());
    }

    protected static boolean isChild(CtClass ctClass, String className) {
        if (ctClass.getName().equals(className)) {
            return true;
        }
        CtClass superclass;
        try {
            superclass = ctClass.getSuperclass();
        } catch (NotFoundException e) {
            logger.trace("not found {}'s super class, ctClass's type is {}", ctClass.getName(), ctClass.getClass().getName());
            return false;
        } catch (Throwable t) {
            logger.info("got an exception when finding {}'s super class", ctClass.getName());
            return false;
        }
        return !(superclass == null || "java.lang.Object".equals(superclass.getName()))
                &&
                (className.equals(superclass.getName()) || isChild(superclass, className));
    }

    protected static boolean isImpl(CtClass ctClass, Class<?> clazz) {
        return isImpl(ctClass, clazz.getName());
    }

    protected static boolean isImpl(CtClass ctClass, String interfaceName) {
        if (ctClass.getName().equals(interfaceName)) {
            return true;
        }
        CtClass[] interfaces;
        try {
            interfaces = ctClass.getInterfaces();
        } catch (NotFoundException e) {
            return false;
        }
        for (CtClass anInterface : interfaces) {
            if (interfaceName.equals(anInterface.getName()) || isImpl(anInterface, interfaceName)) {
                return true;
            }
        }
        try {
            CtClass superclass = ctClass.getSuperclass();
            return !(superclass == null || "java.lang.Object".equals(superclass.getName()))
                    &&
                    isImpl(superclass, interfaceName);
        } catch (NotFoundException e) {
            logger.trace("not found super class, msg: {}", e.getMessage());
        }
        return false;
    }

    public void doWeave(CtClass ctClass) {
        assert ctClass != null;
        if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    public MethodRewriteHandler getHandler() {
        return handler;
    }

    public void setHandler(MethodRewriteHandler handler) {
        this.handler = handler;
    }

    public MethodRewriteHandler addLast(MethodRewriteHandler handler) {
        MethodRewriteHandler theHandler = this.getHandler();
        MethodRewriteHandler tail = this;
        while (theHandler != null) {
            tail = theHandler;
            theHandler = theHandler.getHandler();
        }
        tail.setHandler(handler);
        return handler;
    }
}

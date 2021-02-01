package cn.howardliu.monitor.cynomys.agent.transform.handler;

import cn.howardliu.monitor.cynomys.agent.transform.MethodRewriteHandler;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-4-13
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class DruidDataSourceHandler extends DataSourceHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void doWeave(CtClass ctClass) {
        if (isDruidDataSource(ctClass)) {
            logger.info("begin to wrap DruidDataSource");
            doWeaveConstructNoneParams(ctClass);
            doWeaveConstruct(ctClass);
            doWeaveInit(ctClass);
        } else if (this.getHandler() != null) {
            this.getHandler().doWeave(ctClass);
        }
    }

    private void doWeaveConstructNoneParams(CtClass ctClass) {
        doWeaveConstruct(ctClass, new CtClass[]{});
    }

    private void doWeaveConstruct(CtClass ctClass) {
        doWeaveConstruct(ctClass, new CtClass[]{CtClass.booleanType});
    }

    private void doWeaveConstruct(CtClass ctClass, CtClass[] ctClasses) {
        String desc = null;
        try {
            desc = Descriptor.ofConstructor(ctClasses);
        } catch (Throwable t) {
            System.err.println("SKIPPED Constructor in " + ctClass.getName() + ", the reason is " + t.getMessage());
            t.printStackTrace();
        }
        if (desc == null) {
            return;
        }
        try {
            CtConstructor constructor = ctClass.getConstructor(desc);
            constructor.insertAfter(
                    "cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapperHelper.registerCommonDataSource($0.getClass().getName() + \"@\" + System.identityHashCode($0), $0);"
            );
        } catch (NotFoundException e) {
            logger.trace("not found Constructor[" + desc + "] in " + ctClass.getName());
        } catch (Throwable t) {
            logger.error(
                    "SKIPPED Constructor[" + desc + "] in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private void doWeaveInit(CtClass ctClass) {
        try {
            CtMethod ctMethod = ctClass.getDeclaredMethod("init");
            ctMethod.insertAfter(
                    "cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper.SINGLETON.fillDataSourceInfo($0);" +
                            "cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapperHelper.registerCommonDataSource($0.getClass().getName() + \"@\" + System.identityHashCode($0), $0);"
            );
        } catch (NotFoundException e) {
            logger.trace("not found init method in " + ctClass.getName());
        } catch (Throwable t) {
            logger.error("SKIPPED init() in " + ctClass.getName() + ", the reason is " + t.getMessage());
        }
    }

    private boolean isDruidDataSource(CtClass ctClass) {
        return isDataSource(ctClass)
                &&
                (
                        "com.alibaba.druid.pool.DruidDataSource".equals(ctClass.getName())
                                ||
                                MethodRewriteHandler.isChild(ctClass, "com.alibaba.druid.pool.DruidDataSource")
                );
    }
}

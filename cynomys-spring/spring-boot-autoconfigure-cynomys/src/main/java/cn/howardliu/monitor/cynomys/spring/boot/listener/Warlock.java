//package cn.howardliu.monitor.cynomys.spring.boot.listener;
//
//import cn.howardliu.monitor.cynomys.agent.transform.MonitoringTransformer;
//import cn.howardliu.monitor.cynomys.common.CommonParameters;
//import javassist.CtClass;
//import javassist.CtMethod;
//import javassist.LoaderClassPath;
//import javassist.NotFoundException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.SpringApplicationRunListener;
//import org.springframework.context.ConfigurableApplicationContext;
//import org.springframework.core.env.ConfigurableEnvironment;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.Arrays;
//
///**
// * <br>created at 18-11-28
// *
// * @author liuxh
// * @since 1.0.0
// */
//public class Warlock implements SpringApplicationRunListener {
//    private static final Logger logger = LoggerFactory.getLogger(Warlock.class);
//    private final SpringApplication application;
//
//    public Warlock(SpringApplication application, String[] args) {
//        this.application = application;
//        if (args != null && args.length > 0) {
//            CommonParameters.SPRINGBOOT_INIT_ARGS.addAll(Arrays.asList(args));
//        }
//    }
//
//    @Override
//    public void starting() {
//        try {
//            MonitoringTransformer.getClassPool().insertClassPath(new LoaderClassPath(application.getClassLoader()));
//            CtClass ctClass = MonitoringTransformer.getClassPool().get("org.springframework.web.servlet.FrameworkServlet");
//            doWeave(ctClass, "doHead");
//            doWeave(ctClass, "doGet");
//            doWeave(ctClass, "doPost");
//            doWeave(ctClass, "doPut");
//            doWeave(ctClass, "doDelete");
//            doWeave(ctClass, "doOptions");
//            doWeave(ctClass, "doTrace");
//            logger.debug("ended wrap HttpServlet");
//        } catch (Throwable t) {
//            logger.error("cannot load springboot's classpath", t);
//        }
//    }
//
//
//    private void doWeave(CtClass ctClass, String methodName) {
//        try {
//            CtClass[] params = {
//                    MonitoringTransformer.getClassPool().get(HttpServletRequest.class.getName()),
//                    MonitoringTransformer.getClassPool().get(HttpServletResponse.class.getName())
//            };
//            CtMethod ctMethod = ctClass.getDeclaredMethod(methodName, params);
//            ctMethod.insertBefore(
//                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.begin(Thread.currentThread().getId(), $1, $2);"
//            );
//            ctMethod.addCatch(
//                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.catchBlock(Thread.currentThread().getId(), $1, $2, $e);" +
//                            "throw $e;",
//                    MonitoringTransformer.getClassPool().get("java.lang.Throwable")
//            );
//            ctMethod.insertAfter(
//                    "cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect.end(Thread.currentThread().getId(), $1, $2);"
//            );
//        } catch (NotFoundException e) {
//            logger.info("not found " + methodName + " method in " + ctClass.getName(), e);
//        } catch (Throwable t) {
//            logger.warn("SKIPPED " + methodName + " in " + ctClass.getName() + ", the reason is " + t.getMessage(), t);
//        }
//    }
//
//    @Override
//    public void environmentPrepared(ConfigurableEnvironment environment) {
//
//    }
//
//    @Override
//    public void contextPrepared(ConfigurableApplicationContext context) {
//
//    }
//
//    @Override
//    public void contextLoaded(ConfigurableApplicationContext context) {
//
//    }
//
//    @Override
//    public void started(ConfigurableApplicationContext context) {
//
//    }
//
//    @Override
//    public void running(ConfigurableApplicationContext context) {
//
//    }
//
//    @Override
//    public void failed(ConfigurableApplicationContext context, Throwable exception) {
//
//    }
//}

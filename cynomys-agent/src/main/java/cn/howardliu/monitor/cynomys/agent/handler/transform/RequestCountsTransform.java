/**
 * @Probject Name: monitor_agent
 * @Path: com.wfj.monitor.handler.transformRequestCountsTransform.java
 * @Create By Jack
 * @Create In 2017年1月16日 下午2:29:16
 */
package cn.howardliu.monitor.cynomys.agent.handler.transform;

import cn.howardliu.monitor.cynomys.agent.conf.EnvPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.handler.dto.MonitorLogger;
import cn.howardliu.monitor.cynomys.agent.handler.factory.MonitorLoggerFactory;
import cn.howardliu.monitor.cynomys.agent.handler.handler.RequestHandler;
import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.ProxyFactory;

import javax.servlet.http.HttpServlet;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * 请求类型的计数器，用以启动对服务请求的响应时间统计，并记录URL
 *
 * @Class Name RequestCountsTransform
 * @Author Jack
 * @Create In 2017年1月16日
 */
public class RequestCountsTransform implements ClassFileTransformer {


    private static MonitorLogger logger = MonitorLoggerFactory.getLogger(RequestCountsTransform.class);

    //类缓冲池，用以接收需要处理的类
    private ClassPool classPool;

    public RequestCountsTransform() {
        //初始化并加入Class Path
        classPool = new ClassPool();
        classPool.appendSystemPath();
        try {
            classPool.appendPathList(System.getProperty("java.class.path"));
        } catch (Exception e) {
            logger.error(EnvPropertyConfig.getContextProperty("env.setting.server.error.00001000"), e);
            throw new RuntimeException(e);
        }
    }

    /*
     * 完成对Servlet类的动态代码插入，用以记录
     * 1. 处理时间（包括平均处理时间与最大处理时间）
     * 2. 成功或者处理失败的次数
     * 3. 具体访问的URL
     * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.
     * ClassLoader, java.lang.String, java.lang.Class,
     * java.security.ProtectionDomain, byte[])
     */
    @SuppressWarnings("deprecation")
    @Override
    public byte[] transform(ClassLoader loader, String fullyQualifiedClassName, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        String className = fullyQualifiedClassName.replace("/", ".");

        classPool.appendClassPath(new ByteArrayClassPath(className, classfileBuffer));

        try {
            //装在进入的类，用于判断是否需要处理
            CtClass ctClass = classPool.get(className);

            //冻结的类，不需要处理
            if (ctClass.isFrozen()) {
                logger.info("Skip class " + className + ": is frozen");
                return classfileBuffer;
            }

            //类型、枚举、接口、注解等不需要处理的类
            if (ctClass.isPrimitive() || ctClass.isArray() || ctClass.isAnnotation()
                    || ctClass.isEnum() || ctClass.isInterface()) {
                logger.info("Skip class " + className + ": not a class");
                return classfileBuffer;
            }

            //获取需要处理的HttpServlet子类并开始处理
            if (HttpServlet.class.getName().equalsIgnoreCase(ctClass.getSuperclass().getName())) {
                ProxyFactory factory = new ProxyFactory();
                //设置父类，ProxyFactory将会动态生成一个类，继承该父类
                factory.setSuperclass(ctClass.getClass());

                //设置需要过滤拦截处理的类
                factory.setFilter(new MethodFilter() {
                    @Override
                    public boolean isHandled(java.lang.reflect.Method m) {
                        //仅处理这些有业务意义的方法，其他目前先忽略
                        if (m.getName().equals("doGet") || m.getName().equals("doPost") ||
                                m.getName().equals("doPost") || m.getName().equals("doPut") ||
                                m.getName().equals("doDelete")) {
                            return true;
                        }
                        return false;
                    }
                });

                //设置拦截处理, 开始进行记录
                factory.setHandler(new RequestHandler());
            }

            return ctClass.toBytecode();
        } catch (Exception e) {
            logger.error(EnvPropertyConfig.getContextProperty("env.setting.server.error.00001002"), e);
        }
        return classfileBuffer;
    }

}

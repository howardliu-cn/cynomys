package cn.howardliu.monitor.cynomys.agent.transform;

import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

/**
 * <br>created at 17-4-5
 *
 * @author liuxh
 * @since 0.0.1
 */
public class MonitoringTransformer implements ClassFileTransformer {
    private static ClassPool classPool = ClassPool.getDefault();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private MethodRewriteHandler methodRewriteHandler;

    public MonitoringTransformer() {
        try {
            ClassClassPath classPath = new ClassClassPath(this.getClass());
            classPool.insertClassPath(classPath);
            classPool.appendPathList(System.getProperty("java.class.path"));
            classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
        this.methodRewriteHandler = MethodRewriteHandler.instance();
    }

    public static ClassPool getClassPool() {
        return classPool;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        if (loader == null
                || ApmFilter.isNotNeedInjectClassLoader(loader.getClass().getName())
                || (ApmFilter.isNotNeedInject(className) && !ApmFilter.isNeedInject(className))) {
            logger.trace(className + "is not excluded, SKIPPED!");
            return classfileBuffer;
        }

        if (className.contains("Proxy") || className.contains("CGLIB")) {
            logger.trace(className + "is not proxy, SKIPPED!");
            return classfileBuffer;
        }

        String fullyQualifiedClassName = className.replace("/", ".");
        classPool.appendClassPath(new ByteArrayClassPath(fullyQualifiedClassName, classfileBuffer));

        CtClass ctClass;
        try {
            try {
                ctClass = classPool.get(fullyQualifiedClassName);
            } catch (NotFoundException e) {
                ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
            }

            //冻结的类，不需要处理
            if (ctClass.isFrozen()) {
                logger.trace(className + " is frozen, SKIPPED!");
                return classfileBuffer;
            }

            if (ctClass.isModified()) {
                logger.trace(className + " is modified, SKIPPED!");
                return classfileBuffer;
            }

            //类型、枚举、接口、注解等不需要处理的类
            if (ctClass.isPrimitive()
                    || ctClass.isArray()
                    || ctClass.isAnnotation()
                    || ctClass.isEnum()
                    || ctClass.isInterface()) {
                logger.trace(className + " is not a class, SKIPPED!");
                return classfileBuffer;
            }
            if (className.equals("com/alibaba/druid/pool/DruidConnectionHolder")) {
                final ClassLoader classLoader = com.alibaba.druid.pool.DruidConnectionHolder.class.getClassLoader();
                logger.info("load classloader: {}", classLoader);
                classPool.appendClassPath(new LoaderClassPath(classLoader));
            }
            if (className.equals("com/alibaba/druid/pool/DruidPooledStatement")) {
                final ClassLoader classLoader = com.alibaba.druid.pool.DruidPooledStatement.class.getClassLoader();
                logger.info("load classloader: {}", classLoader);
                classPool.appendClassPath(new LoaderClassPath(classLoader));
            }
            this.methodRewriteHandler.doWeave(ctClass);

            return ctClass.toBytecode();
        } catch (Exception e) {
            logger.error("className: " + className, e);
        }
        return classfileBuffer;
    }
}

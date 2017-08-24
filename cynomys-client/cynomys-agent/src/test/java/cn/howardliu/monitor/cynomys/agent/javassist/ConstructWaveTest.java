package cn.howardliu.monitor.cynomys.agent.javassist;

import javassist.*;
import javassist.bytecode.Descriptor;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ConstructWaveTest {
    private static final Logger logger = LoggerFactory.getLogger(ConstructWaveTest.class);
    private ClassPool classPool;

    @Before
    public void setUp() throws Exception {
        this.classPool = ClassPool.getDefault();
        try {
            this.classPool.appendPathList(System.getProperty("java.class.path"));
            this.classPool.appendClassPath(new LoaderClassPath(ClassLoader.getSystemClassLoader()));
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void test() throws Exception {
        CtClass ctClass = classPool.getCtClass("cn.howardliu.monitor.cynomys.agent.javassist.TestObject");
        CtConstructor constructor = ctClass
                .getConstructor(Descriptor.ofConstructor(new CtClass[]{CtClass.booleanType}));
        constructor.insertBeforeBody("System.out.println(\"insertBeforeBody\");");
        constructor.insertBefore("System.out.println(\"insertBefore\");");
        constructor.insertAfter("System.out.println(\"insertAfter\");");
        Class aClass = ctClass.toClass();
        aClass.newInstance();
    }
}

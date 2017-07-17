/**
 * @Probject Name: monitor_agent
 * @Path: com.wfj.monitor.handler.transformJDBCCountsTransform.java
 * @Create By Jack
 * @Create In 2017年1月16日 下午2:30:38
 */
package cn.howardliu.monitor.cynomys.agent.handler.transform;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * 数据库访问计数器，用以统计数据库类型的访问时间并获取SQL
 *
 * @Class Name JDBCCountsTransform
 * @Author Jack
 * @Create In 2017年1月16日
 */
public class JDBCCountsTransform implements ClassFileTransformer {

    /* (non-Javadoc)
     * @see java.lang.instrument.ClassFileTransformer#transform(java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
     */
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
            ProtectionDomain protectionDomain, byte[] classfileBuffer)
            throws IllegalClassFormatException {
        return null;
    }

}

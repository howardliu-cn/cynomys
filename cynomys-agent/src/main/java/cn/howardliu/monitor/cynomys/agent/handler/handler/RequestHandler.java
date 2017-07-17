/**
 * @Probject Name: monitor_agent
 * @Path: com.wfj.monitor.handler.handlerRequestHandler.java
 * @Create By Jack
 * @Create In 2017年1月16日 下午4:36:00
 */
package cn.howardliu.monitor.cynomys.agent.handler.handler;

import cn.howardliu.monitor.cynomys.agent.util.ThreadInforUtil;
import javassist.util.proxy.MethodHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

/**
 * @Class Name RequestHandler
 * @Author Jack
 * @Create In 2017年1月16日
 */
public class RequestHandler implements MethodHandler {

    /*
     * (non-Javadoc)
     *
     * @see javassist.util.proxy.MethodHandler#invoke(java.lang.Object,
     * java.lang.reflect.Method, java.lang.reflect.Method, java.lang.Object[])
     */
    @Override
    public Object invoke(Object self, Method thisMethod, java.lang.reflect.Method proceed, Object[] args)
            throws Throwable {
        // 拦截后前置处理，改写name属性的内容
        // 1. 初始化所需变量 ----7
        Object result;
        HttpServletRequest req;
        HttpServletResponse resp;

        for (Object item : args) {
            if (item instanceof HttpServletRequest) {
                req = (HttpServletRequest) item;
            }
            if (item instanceof HttpServletResponse) {
                resp = (HttpServletResponse) item;
            }
        }
        //2. 记录处理起始时间
        long beginTime = System.currentTimeMillis();
        long beginCupTime = ThreadInforUtil.getCurrentThreadCpuTime();
        // 实际情况可根据需求修改

        result = proceed.invoke(self, args);

        return result;
    }

}

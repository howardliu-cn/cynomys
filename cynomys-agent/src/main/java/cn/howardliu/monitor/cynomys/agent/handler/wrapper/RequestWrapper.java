/**
 * @Probject Name: WFJ-Base-Server-Dev
 * @Path: com.wfj.netty.monitor.handler.wrapperRequestWrapper.java
 * @Create By Jack
 * @Create In 2016年7月26日 下午4:59:34
 */
package cn.howardliu.monitor.cynomys.agent.handler.wrapper;

import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.dto.Counter;
import cn.howardliu.monitor.cynomys.agent.dto.CounterError;
import cn.howardliu.monitor.cynomys.agent.dto.ThreadInformations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Class Name RequestWrapper
 * @Author Jack
 * @Create In 2016年7月26日
 */
public class RequestWrapper {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static final RequestWrapper SINGLETON = new RequestWrapper();

    private Counter httpCounter;
    private Counter errorCounter;
    private ServletContext servletContext;
    private boolean jboss;
    private boolean glassfish;
    private boolean weblogic;
    private boolean jonas;

    private RequestWrapper() {
        super();
        this.httpCounter = new Counter(Counter.HTTP_COUNTER_NAME, "dbweb.png");
        this.errorCounter = new Counter(Counter.ERROR_COUNTER_NAME, "error.png");
        this.servletContext = null;
    }

    /**
     * 初始化关键变量
     *
     * @param context void
     * @Methods Name initServletContext
     * @Create In 2016年7月27日 By Jack
     */
    public void initServletContext(ServletContext context) {
        assert context != null;
        this.servletContext = context;
        final String serverInfo = servletContext.getServerInfo();

        Parameters.initialize(context);

        jboss = serverInfo.contains("JBoss") || serverInfo.contains("WildFly");
        glassfish = serverInfo.contains("GlassFish")
                || serverInfo.contains("Sun Java System Application Server");
        weblogic = serverInfo.contains("WebLogic");
        jonas = System.getProperty("jonas.name") != null;
    }

    /**
     * 请求执行记录器
     *
     * @param httpRequest
     * @param httpResponse
     * @param startCpuTime
     * @param start
     * @Methods Name doExecute
     * @Create In 2016年7月27日 By Jack
     */
    public void doExecute(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
            long startCpuTime, long start) {

        final CounterServletResponseWrapper wrappedResponse = new CounterServletResponseWrapper(
                httpResponse);
        final HttpServletRequest wrappedRequest = createRequestWrapper(httpRequest,
                wrappedResponse);

        // final long startCpuTime =
        // ThreadInformations.getCurrentThreadCpuTime();
        boolean systemError = false;
        String requestName = getRequestName(wrappedRequest);
        final String completeRequestName = getCompleteRequestName(wrappedRequest, true);

        if (httpCounter == null) {
            // 没有初始化对象的处理
            return; // NOPMD
        }
        try {

            httpCounter.bindContext(requestName, completeRequestName, httpRequest.getRemoteUser(), startCpuTime);
            httpRequest.setAttribute(CounterError.REQUEST_KEY, completeRequestName);
            CounterError.bindRequest(httpRequest);

            // 计算执行时间
            final long duration = Math.max(System.currentTimeMillis() - start, 0);
            final long cpuUsedMillis = (ThreadInformations.getCurrentThreadCpuTime() - startCpuTime) / 1000000;
            // 计算响应处理时间
            final int responseSize = wrappedResponse.getDataLength();

            if (wrappedResponse.getStatus() >= HttpServletResponse.SC_BAD_REQUEST
                    && wrappedResponse.getStatus() != HttpServletResponse.SC_UNAUTHORIZED) {
                // SC_UNAUTHORIZED (401) is not an error, it is the first
                // handshake of a Basic (or Digest) Auth (issue 455)
                systemError = true;
                errorCounter
                        .addRequestForSystemError("HTTP_ERROR_" + wrappedResponse.getStatus(), duration, cpuUsedMillis,
                                null);
            }

            // 记录处理具体信息
            httpCounter.addRequest(requestName, duration, cpuUsedMillis, systemError, responseSize);

        } finally {
            httpCounter.unbindContext();
            CounterError.unbindRequest();
        }

    }

    /**
     * 执行异常处理
     *
     * @param code
     * @param startCpuTime
     * @param start        void
     * @Methods Name doError
     * @Create In 2016年7月27日 By Jack
     */
    public void doError(int code, long startCpuTime, long start) {
        // 计算执行时间
        final long duration = Math.max(System.currentTimeMillis() - start, 0);
        final long cpuUsedMillis = (ThreadInformations.getCurrentThreadCpuTime() - startCpuTime) / 1000000;

        errorCounter.addRequestForSystemError("HTTP_ERROR_" + code, duration, cpuUsedMillis, null);

    }

    /**
     * 组装HttpServletRequestWrapper，增加监控内容
     *
     * @param request  ： HttpServletRequest
     * @param response ： HttpServletResponse
     * @return HttpServletRequest
     * @throws java.io.IOException
     * @Methods Name createRequestWrapper
     * @Create In 2016年7月26日 By Jack
     */
    protected HttpServletRequest createRequestWrapper(HttpServletRequest request,
            HttpServletResponse response) {
        HttpServletRequest wrappedRequest = JspWrapper.createHttpRequestWrapper(request, response);
        try {
            final PayloadNameRequestWrapper payloadNameRequestWrapper = new PayloadNameRequestWrapper(
                    wrappedRequest);
            payloadNameRequestWrapper.initialize();
            if (payloadNameRequestWrapper.getPayloadRequestType() != null) {
                wrappedRequest = payloadNameRequestWrapper;
            }

        } catch (IOException e) {
            log.error("Inbound Request Wrapper Error: " + e.getMessage());
        }

        return wrappedRequest;
    }

    /**
     * 获取请求名称
     *
     * @param request
     * @return String
     * @Methods Name getRequestName
     * @Create In 2016年7月26日 By Jack
     */
    protected String getRequestName(HttpServletRequest request) {
        return getCompleteRequestName(request, false);
    }

    /**
     * 组装请求成功的名称
     *
     * @param httpRequest
     * @param includeQueryString
     * @return String
     * @Methods Name getCompleteRequestName
     * @Create In 2016年7月26日 By Jack
     */
    private static String getCompleteRequestName(HttpServletRequest httpRequest,
            boolean includeQueryString) {
        String tmp = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        final int lastIndexOfSemiColon = tmp.lastIndexOf(';');
        if (lastIndexOfSemiColon != -1) {
            tmp = tmp.substring(0, lastIndexOfSemiColon);
        }
        final String method;
        if ("XMLHttpRequest".equals(httpRequest.getHeader("X-Requested-With"))) {
            method = "ajax " + httpRequest.getMethod();
        } else {
            method = httpRequest.getMethod();
        }
        if (!includeQueryString) {
            // Check payload request to support GWT, SOAP, and XML-RPC statistic
            // gathering
            if (httpRequest instanceof PayloadNameRequestWrapper) {
                final PayloadNameRequestWrapper wrapper = (PayloadNameRequestWrapper) httpRequest;
                return tmp + wrapper.getPayloadRequestName() + ' '
                        + wrapper.getPayloadRequestType();
            }

            return tmp + ' ' + method;
        }
        final String queryString = httpRequest.getQueryString();
        if (queryString == null) {
            return tmp + ' ' + method;
        }
        return tmp + '?' + queryString + ' ' + method;
    }

    /**
     * @Return the Counter httpCounter
     */
    public Counter getHttpCounter() {
        return httpCounter;
    }

    /**
     * @Param Counter httpCounter to set
     */
    public void setHttpCounter(Counter httpCounter) {
        this.httpCounter = httpCounter;
    }

    /**
     * @Return the ServletContext servletContext
     */
    public ServletContext getServletContext() {
        return servletContext;
    }

    /**
     * @Param ServletContext servletContext to set
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /**
     * @Return the boolean jboss
     */
    public boolean isJboss() {
        return jboss;
    }

    /**
     * @Param boolean jboss to set
     */
    public void setJboss(boolean jboss) {
        this.jboss = jboss;
    }

    /**
     * @Return the boolean glassfish
     */
    public boolean isGlassfish() {
        return glassfish;
    }

    /**
     * @Param boolean glassfish to set
     */
    public void setGlassfish(boolean glassfish) {
        this.glassfish = glassfish;
    }

    /**
     * @Return the boolean weblogic
     */
    public boolean isWeblogic() {
        return weblogic;
    }

    /**
     * @Param boolean weblogic to set
     */
    public void setWeblogic(boolean weblogic) {
        this.weblogic = weblogic;
    }

    /**
     * @Return the boolean jonas
     */
    public boolean isJonas() {
        return jonas;
    }

    /**
     * @Param boolean jonas to set
     */
    public void setJonas(boolean jonas) {
        this.jonas = jonas;
    }

    /**
     * @Return the Counter errorCounter
     */
    public Counter getErrorCounter() {
        return errorCounter;
    }

    /**
     * @Param Counter errorCounter to set
     */
    public void setErrorCounter(Counter errorCounter) {
        this.errorCounter = errorCounter;
    }

}

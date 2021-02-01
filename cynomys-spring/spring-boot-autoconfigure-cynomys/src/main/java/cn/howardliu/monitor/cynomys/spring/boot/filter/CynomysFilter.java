package cn.howardliu.monitor.cynomys.spring.boot.filter;

import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.RequestWrapper;
import cn.howardliu.monitor.cynomys.agent.transform.aspect.RequestAspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * <br>created at 18-11-28
 *
 * @author liuxh
 * @since 1.0.0
 */
public class CynomysFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(CynomysFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        RequestAspect.RequestDataWrapper wrapper = null;
        try {
            SLACounter.addSumInboundRequestCounts();
            long tid = Thread.currentThread().getId();
            wrapper = new RequestAspect.RequestDataWrapper(tid);
        } catch (Throwable t) {
            logger.error("request counter error", t);
        }
        try {
            chain.doFilter(request, response);
        } catch (Throwable t) {
            if (wrapper != null) {
                wrapper.setCause(t);
            }
            throw t;
        } finally {
            counter(request, response, wrapper);
        }
    }

    private void counter(ServletRequest request, ServletResponse response, RequestAspect.RequestDataWrapper wrapper) {
        if (wrapper == null) {
            return;
        }
        try {
            long endTime = System.currentTimeMillis();
            long startTime = wrapper.getStartTime();
            long startThreadCupTime = wrapper.getStartThreadCupTime();
            long duration = endTime - startTime;
            int status = 200;
            if (response instanceof HttpServletResponse) {
                status = ((HttpServletResponse) response).getStatus();
            } else if (wrapper.getCause() != null) {
                status = 500;
            }
            SLACounter.addHttpStatus(status);
            Throwable cause = wrapper.getCause();
            if (cause == null) {
                if (status < SC_BAD_REQUEST || status == SC_UNAUTHORIZED) {
                    SLACounter.addSumDealRequestCounts();
                    SLACounter.setPeerDealRequestTime(duration);
                    SLACounter.addSumDealRequestTime(duration);
                } else {
                    SLACounter.addSumErrDealRequestCounts();
                    SLACounter.addSumErrDealRequestTime(duration);
                }
                SLACounter.addSumOutboundRequestCounts();
                if (request instanceof HttpServletRequest && response instanceof HttpServletResponse) {
                    RequestWrapper.SINGLETON.doExecute((HttpServletRequest) request, (HttpServletResponse) response, startThreadCupTime, startTime);
                }
            } else {
                SLACounter.addSumErrDealRequestCounts();
                SLACounter.addSumErrDealRequestTime(duration);
                RequestWrapper.SINGLETON.doError(status, startThreadCupTime, startTime);
            }
        } catch (Throwable t) {
            logger.error("request counter error", t);
        }
    }

    @Override
    public void destroy() {

    }
}

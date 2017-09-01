package cn.howardliu.monitor.cynomys.agent.transform.aspect;

import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.RequestWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static cn.howardliu.monitor.cynomys.common.Constant.HEADER_SERVER_TAG;
import static cn.howardliu.monitor.cynomys.common.Constant.THIS_TAG;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;

/**
 * <br>created at 17-4-14
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class RequestAspect {
    private static final Logger logger = LoggerFactory.getLogger(RequestAspect.class);
    private static final Map<Long, RequestDataWrapper> REQUEST_COUNTER_MAP = new ConcurrentHashMap<>();

    private RequestAspect() {
    }

    public static void begin(long tid, HttpServletRequest request, HttpServletResponse response) {
        assert request != null;
        assert response != null;
        response.setHeader(HEADER_SERVER_TAG, THIS_TAG);
        if (REQUEST_COUNTER_MAP.containsKey(tid)) {
            return;
        } else {
            REQUEST_COUNTER_MAP.put(tid, new RequestDataWrapper(tid));
        }
        SLACounter.addSumInboundRequestCounts();
    }

    public static void catchBlock(long tid, HttpServletRequest request, HttpServletResponse response,
            Throwable cause) {
        assert request != null;
        assert response != null;
        assert cause != null;
        RequestDataWrapper wrapper = REQUEST_COUNTER_MAP.get(tid);
        if (wrapper == null) {
            return;
        }
        wrapper.setCause(cause);
    }

    public static void end(long tid, HttpServletRequest request, HttpServletResponse response) {
        assert request != null;
        assert response != null;
        long endTime = System.currentTimeMillis();
        RequestDataWrapper wrapper = REQUEST_COUNTER_MAP.remove(tid);
        if (wrapper == null) {
            return;
        }
        long startTime = wrapper.getStartTime();
        long startThreadCupTime = wrapper.getStartThreadCupTime();
        long duration = endTime - startTime;
        int status = response.getStatus();
        SLACounter.addHttpStatus(status);
        //noinspection ThrowableResultOfMethodCallIgnored
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
            RequestWrapper.SINGLETON.doExecute(request, response, startThreadCupTime, startTime);
        } else {
            SLACounter.addSumErrDealRequestCounts();
            SLACounter.addSumErrDealRequestTime(duration);
            RequestWrapper.SINGLETON.doError(response.getStatus(), startThreadCupTime, startTime);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("the request counter is : " + SLACounter.instance());
        }
    }

    static class RequestDataWrapper extends RunnerWrapper {
        RequestDataWrapper(long tid) {
            super(tid);
        }
    }
}

/**
 * @Probject Name: WFJ-Base-Server-Dev
 * @Path: com.wfj.netty.monitor.dtoRequestInfo.java
 * @Create By Jack
 * @Create In 2016年7月27日 上午10:39:41
 */
package cn.howardliu.monitor.cynomys.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class Name RequestInfo
 * @Author Jack
 * @Create In 2016年7月27日
 */
public class RequestInfo extends BaseInfo {
    private static final RequestInfo INSTANCE = new RequestInfo();
    private List<CounterRequest> requestDetails = new ArrayList<>();
    private List<CounterRequest> errorDetails = new ArrayList<>();

    private RequestInfo() {
    }

    public static RequestInfo instance() {
        return INSTANCE;
    }

    /**
     * @Return the List<CounterRequest> requestDetails
     */
    public List<CounterRequest> getRequestDetails() {
        return requestDetails;
    }

    /**
     * @Param List<CounterRequest> requestDetails to set
     */
    public void setRequestDetails(List<CounterRequest> requestDetails) {
        this.requestDetails = requestDetails;
    }

    /**
     * @Return the List<CounterRequest> errorDetails
     */
    public List<CounterRequest> getErrorDetails() {
        return errorDetails;
    }

    /**
     * @Param List<CounterRequest> errorDetails to set
     */
    public void setErrorDetails(List<CounterRequest> errorDetails) {
        this.errorDetails = errorDetails;
    }
}

/**
 * @Probject Name: WFJ-Base-Server-Dev
 * @Path: com.wfj.netty.monitor.dtoRequestInfo.java
 * @Create By Jack
 * @Create In 2016年7月27日 上午10:39:41
 */
package cn.howardliu.monitor.cynomys.agent.dto;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Class Name RequestInfo
 * @Author Jack
 * @Create In 2016年7月27日
 */
public class RequestInfo {

    private String sysCode;

    private String sysName;

    private String sysIPS;

    private String updateDate;

    private List<CounterRequest> requestDetails;

    private List<CounterRequest> errorDetails;

    private static RequestInfo REQUEST_INFO;

    private RequestInfo() {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        this.sysCode = "";
        this.sysName = "";
        this.sysIPS = "";
        this.updateDate = df.format(new Date());
        this.requestDetails = new ArrayList<>();
        this.errorDetails = new ArrayList<>();
    }

    /**
     * 获取实例
     *
     * @return RequestInfo
     * @Methods Name instance
     * @Create In 2016年7月27日 By Jack
     */
    public static RequestInfo instance() {
        if (REQUEST_INFO != null) {
            return REQUEST_INFO;
        } else {
            REQUEST_INFO = new RequestInfo();
            return REQUEST_INFO;
        }
    }

    /**
     * @Return the String sysCode
     */
    public String getSysCode() {
        return sysCode;
    }

    /**
     * @Param String sysCode to set
     */
    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    /**
     * @Return the String sysName
     */
    public String getSysName() {
        return sysName;
    }

    /**
     * @Param String sysName to set
     */
    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    /**
     * @Return the String sysIPS
     */
    public String getSysIPS() {
        return sysIPS;
    }

    /**
     * @Param String sysIPS to set
     */
    public void setSysIPS(String sysIPS) {
        this.sysIPS = sysIPS;
    }

    /**
     * @Return the String updateDate
     */
    public String getUpdateDate() {
        return updateDate;
    }

    /**
     * @Param String updateDate to set
     */
    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
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

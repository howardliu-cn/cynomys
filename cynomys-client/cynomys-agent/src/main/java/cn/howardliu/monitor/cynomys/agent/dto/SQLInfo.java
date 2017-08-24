/**
 * @Probject Name: servlet-monitor-dev-sql
 * @Path: com.wfj.netty.servlet.dtoSQLInfo.java
 * @Create By Jack
 * @Create In 2016年3月29日 下午1:18:20
 */
package cn.howardliu.monitor.cynomys.agent.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @Class Name SQLInfo
 * @Author Jack
 * @Create In 2016年3月29日
 */
public class SQLInfo {

    private String sysCode;

    private String sysName;

    private String sysIPS;

    private String dataBaseVersion;

    private String dataSourceDetails;

    private String updateDate;

    private Integer active_connection_count;
    private Integer used_connection_count;
    private Long transaction_count;
    private Integer active_thread_count;
    private Integer running_build_count;
    private Integer build_queue_length;

    private List<CounterRequest> sqlDetails;

    private static SQLInfo sqlInfo;

    private SQLInfo() {
        this.dataBaseVersion = "";
        this.dataSourceDetails = "";
        this.active_connection_count = 0;
        this.used_connection_count = 0;
        this.transaction_count = (long) 0;
        this.active_thread_count = 0;
        this.running_build_count = 0;
        this.build_queue_length = 0;
        this.sqlDetails = new ArrayList<>();
    }

    /**
     * 获取有效实例
     *
     * @return SQLInfo
     * @Methods Name instance
     * @Create In 2016年4月5日 By Jack
     */
    public static SQLInfo instance() {
        if (sqlInfo != null) {
            return sqlInfo;
        } else {
            sqlInfo = new SQLInfo();
            return sqlInfo;
        }
    }

    /**
     * @Return the String dataBaseVersion
     */
    public String getDataBaseVersion() {
        return dataBaseVersion;
    }

    /**
     * @Param String dataBaseVersion to set
     */
    public void setDataBaseVersion(String dataBaseVersion) {
        this.dataBaseVersion = dataBaseVersion;
    }

    /**
     * @Return the String dataSourceDetails
     */
    public String getDataSourceDetails() {
        return dataSourceDetails;
    }

    /**
     * @Param String dataSourceDetails to set
     */
    public void setDataSourceDetails(String dataSourceDetails) {
        this.dataSourceDetails = dataSourceDetails;
    }

    /**
     * @Return the Integer active_connection_count
     */
    public Integer getActive_connection_count() {
        return active_connection_count;
    }

    /**
     * @Param Integer active_connection_count to set
     */
    public void setActive_connection_count(Integer active_connection_count) {
        this.active_connection_count = active_connection_count;
    }

    /**
     * @Return the Integer used_connection_count
     */
    public Integer getUsed_connection_count() {
        return used_connection_count;
    }

    /**
     * @Param Integer used_connection_count to set
     */
    public void setUsed_connection_count(Integer used_connection_count) {
        this.used_connection_count = used_connection_count;
    }

    /**
     * @Return the Long transaction_count
     */
    public Long getTransaction_count() {
        return transaction_count;
    }

    /**
     * @Param Long transaction_count to set
     */
    public void setTransaction_count(Long transaction_count) {
        this.transaction_count = transaction_count;
    }

    /**
     * @Return the Integer active_thread_count
     */
    public Integer getActive_thread_count() {
        return active_thread_count;
    }

    /**
     * @Param Integer active_thread_count to set
     */
    public void setActive_thread_count(Integer active_thread_count) {
        this.active_thread_count = active_thread_count;
    }

    /**
     * @Return the Integer running_build_count
     */
    public Integer getRunning_build_count() {
        return running_build_count;
    }

    /**
     * @Param Integer running_build_count to set
     */
    public void setRunning_build_count(Integer running_build_count) {
        this.running_build_count = running_build_count;
    }

    /**
     * @Return the Integer build_queue_length
     */
    public Integer getBuild_queue_length() {
        return build_queue_length;
    }

    /**
     * @Param Integer build_queue_length to set
     */
    public void setBuild_queue_length(Integer build_queue_length) {
        this.build_queue_length = build_queue_length;
    }

    /**
     * @Return the List<CounterRequest> sqlDetails
     */
    public List<CounterRequest> getSqlDetails() {
        return sqlDetails;
    }

    /**
     * @Param List<CounterRequest> sqlDetails to set
     */
    public void setSqlDetails(List<CounterRequest> sqlDetails) {
        this.sqlDetails = sqlDetails;
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
     * @Return the SQLInfo sqlInfo
     */
    public static SQLInfo getSqlInfo() {
        return sqlInfo;
    }

    /**
     * @Param SQLInfo sqlInfo to set
     */
    public static void setSqlInfo(SQLInfo sqlInfo) {
        SQLInfo.sqlInfo = sqlInfo;
    }


    public SQLInfo(String sysCode, String sysName, String sysIPS, String dataBaseVersion, String dataSourceDetails,
            String updateDate, Integer active_connection_count, Integer used_connection_count,
            Long transaction_count, Integer active_thread_count, Integer running_build_count,
            Integer build_queue_length, List<CounterRequest> sqlDetails) {
        super();
        this.sysCode = sysCode;
        this.sysName = sysName;
        this.sysIPS = sysIPS;
        this.dataBaseVersion = dataBaseVersion;
        this.dataSourceDetails = dataSourceDetails;
        this.updateDate = updateDate;
        this.active_connection_count = active_connection_count;
        this.used_connection_count = used_connection_count;
        this.transaction_count = transaction_count;
        this.active_thread_count = active_thread_count;
        this.running_build_count = running_build_count;
        this.build_queue_length = build_queue_length;
        this.sqlDetails = sqlDetails;
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

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "SQLInfo [sysCode=" + sysCode + ", sysName=" + sysName + ", sysIPS=" + sysIPS + ", dataBaseVersion=" + dataBaseVersion + ", dataSourceDetails=" + dataSourceDetails + ", updateDate="
                + updateDate + ", active_connection_count=" + active_connection_count + ", used_connection_count=" + used_connection_count + ", transaction_count=" + transaction_count
                + ", active_thread_count=" + active_thread_count + ", running_build_count=" + running_build_count + ", build_queue_length=" + build_queue_length + ", sqlDetails=" + sqlDetails + "]";
    }


}

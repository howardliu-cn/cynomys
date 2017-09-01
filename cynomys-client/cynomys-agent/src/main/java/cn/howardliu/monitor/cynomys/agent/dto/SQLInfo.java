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
public class SQLInfo extends BaseInfo {
    private static final SQLInfo INSTANCE = new SQLInfo();
    private String dataBaseVersion = "";
    private String dataSourceDetails = "";
    private Integer activeConnectionCount = 0;
    private Integer usedConnectionCount = 0;
    private Long transactionCount = 0L;
    private Integer activeThreadCount = 0;
    private Integer runningBuildCount = 0;
    private Integer buildQueueLength = 0;
    private List<CounterRequest> sqlDetails = new ArrayList<>();

    private SQLInfo() {
    }

    public static SQLInfo instance() {
        return INSTANCE;
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
     * @Return the Integer activeConnectionCount
     */
    public Integer getActive_connection_count() {
        return activeConnectionCount;
    }

    /**
     * @Param Integer activeConnectionCount to set
     */
    public void setActiveConnectionCount(Integer activeConnectionCount) {
        this.activeConnectionCount = activeConnectionCount;
    }

    /**
     * @Return the Integer usedConnectionCount
     */
    public Integer getUsed_connection_count() {
        return usedConnectionCount;
    }

    /**
     * @Param Integer usedConnectionCount to set
     */
    public void setUsedConnectionCount(Integer usedConnectionCount) {
        this.usedConnectionCount = usedConnectionCount;
    }

    /**
     * @Return the Long transactionCount
     */
    public Long getTransaction_count() {
        return transactionCount;
    }

    /**
     * @Param Long transactionCount to set
     */
    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    /**
     * @Return the Integer activeThreadCount
     */
    public Integer getActive_thread_count() {
        return activeThreadCount;
    }

    /**
     * @Param Integer activeThreadCount to set
     */
    public void setActiveThreadCount(Integer activeThreadCount) {
        this.activeThreadCount = activeThreadCount;
    }

    /**
     * @Return the Integer runningBuildCount
     */
    public Integer getRunning_build_count() {
        return runningBuildCount;
    }

    /**
     * @Param Integer runningBuildCount to set
     */
    public void setRunningBuildCount(Integer runningBuildCount) {
        this.runningBuildCount = runningBuildCount;
    }

    /**
     * @Return the Integer buildQueueLength
     */
    public Integer getBuild_queue_length() {
        return buildQueueLength;
    }

    /**
     * @Param Integer buildQueueLength to set
     */
    public void setBuildQueueLength(Integer buildQueueLength) {
        this.buildQueueLength = buildQueueLength;
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

    @Override
    public String toString() {
        return "SQLInfo [sysCode=" + sysCode + ", sysName=" + sysName + ", sysIPS=" + sysIPS + ", dataBaseVersion=" + dataBaseVersion + ", dataSourceDetails=" + dataSourceDetails + ", updateDate="
                + updateDate + ", activeConnectionCount=" + activeConnectionCount + ", usedConnectionCount=" + usedConnectionCount + ", transactionCount=" + transactionCount
                + ", activeThreadCount=" + activeThreadCount + ", runningBuildCount=" + runningBuildCount + ", buildQueueLength=" + buildQueueLength + ", sqlDetails=" + sqlDetails + "]";
    }


}

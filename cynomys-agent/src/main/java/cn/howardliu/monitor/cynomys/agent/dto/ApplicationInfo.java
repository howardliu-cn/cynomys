/**
 * @Probject Name: netty-wfj-base
 * @Path: com.wfj.netty.monitor.dtoApplicationInfo.java
 * @Create By Jack
 * @Create In 2015年8月26日 下午7:03:40
 */
package cn.howardliu.monitor.cynomys.agent.dto;


/**
 * @Class Name ApplicationInfo
 * @Author Jack
 * @Create In 2015年8月26日
 */
public class ApplicationInfo {
    private String serverTag;

    private String name;

    private String version;

    private String desc;

    private String status;

    private SystemInfo sysInfo;

    private String updateTime;

    private Long sumInboundReqCounts;

    private Long sumOutboundReqCounts;

    private Long sumDealReqCounts;

    private Long sumDealReqTime;

    private Long peerDealReqTime;

    private Long sumErrDealReqCounts;

    private Long sumErrDealReqTime;

    private String serverName;

    private String serverVersion;

    private Long transactionCount;

    private String pid;

    private String dataBaseVersion;

    private String dataSourceDetails;

    private Long unixOpenFileDescriptorCount;

    private Long unixMaxFileDescriptorCount;

    private String startupDate;

    public String getServerTag() {
        return serverTag;
    }

    public void setServerTag(String serverTag) {
        this.serverTag = serverTag;
    }

    /**
     * @Return the String startupDate
     */
    public String getStartupDate() {
        return startupDate;
    }

    /**
     * @Param String startupDate to set
     */
    public void setStartupDate(String startupDate) {
        this.startupDate = startupDate;
    }

    /**
     * @Return the Long unixOpenFileDescriptorCount
     */
    public Long getUnixOpenFileDescriptorCount() {
        return unixOpenFileDescriptorCount;
    }

    /**
     * @Param Long unixOpenFileDescriptorCount to set
     */
    public void setUnixOpenFileDescriptorCount(Long unixOpenFileDescriptorCount) {
        this.unixOpenFileDescriptorCount = unixOpenFileDescriptorCount;
    }

    /**
     * @Return the Long unixMaxFileDescriptorCount
     */
    public Long getUnixMaxFileDescriptorCount() {
        return unixMaxFileDescriptorCount;
    }

    /**
     * @Param Long unixMaxFileDescriptorCount to set
     */
    public void setUnixMaxFileDescriptorCount(Long unixMaxFileDescriptorCount) {
        this.unixMaxFileDescriptorCount = unixMaxFileDescriptorCount;
    }

    /**
     * @Return the String pid
     */
    public String getPid() {
        return pid;
    }

    /**
     * @Param String pid to set
     */
    public void setPid(String pid) {
        this.pid = pid;
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
     * @Return the Long transactionCount
     */
    public Long getTransactionCount() {
        return transactionCount;
    }

    /**
     * @Param Long transactionCount to set
     */
    public void setTransactionCount(Long transactionCount) {
        this.transactionCount = transactionCount;
    }

    /**
     * @Return the String serverNameString
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * @Param String serverNameString to set
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * @Return the String serverVersionString
     */
    public String getServerVersion() {
        return serverVersion;
    }

    /**
     * @Param String serverVersionString to set
     */
    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    /**
     * @Return the Long sumErrDealReqCounts
     */
    public Long getSumErrDealReqCounts() {
        return sumErrDealReqCounts;
    }

    /**
     * @Param Long sumErrDealReqCounts to set
     */
    public void setSumErrDealReqCounts(Long sumErrDealReqCounts) {
        this.sumErrDealReqCounts = sumErrDealReqCounts;
    }

    /**
     * @Return the Long sumErrDealReqTime
     */
    public Long getSumErrDealReqTime() {
        return sumErrDealReqTime;
    }

    /**
     * @Param Long sumErrDealReqTime to set
     */
    public void setSumErrDealReqTime(Long sumErrDealReqTime) {
        this.sumErrDealReqTime = sumErrDealReqTime;
    }

    /**
     * @Return the String updateTime
     */
    public String getUpdateTime() {
        return updateTime;
    }

    /**
     * @Param String updateTime to set
     */
    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @Return the Long sumInboundReqCounts
     */
    public Long getSumInboundReqCounts() {
        return sumInboundReqCounts;
    }

    /**
     * @Param Long sumInboundReqCounts to set
     */
    public void setSumInboundReqCounts(Long sumInboundReqCounts) {
        this.sumInboundReqCounts = sumInboundReqCounts;
    }

    /**
     * @Return the Long sumOutboundReqCounts
     */
    public Long getSumOutboundReqCounts() {
        return sumOutboundReqCounts;
    }

    /**
     * @Param Long sumOutboundReqCounts to set
     */
    public void setSumOutboundReqCounts(Long sumOutboundReqCounts) {
        this.sumOutboundReqCounts = sumOutboundReqCounts;
    }

    /**
     * @Return the Long sumDealReqCounts
     */
    public Long getSumDealReqCounts() {
        return sumDealReqCounts;
    }

    /**
     * @Param Long sumDealReqCounts to set
     */
    public void setSumDealReqCounts(Long sumDealReqCounts) {
        this.sumDealReqCounts = sumDealReqCounts;
    }

    /**
     * @Return the Long sumDealReqTime
     */
    public Long getSumDealReqTime() {
        return sumDealReqTime;
    }

    /**
     * @Param Long sumDealReqTime to set
     */
    public void setSumDealReqTime(Long sumDealReqTime) {
        this.sumDealReqTime = sumDealReqTime;
    }

    /**
     * @Return the Long peerDealReqTime
     */
    public Long getPeerDealReqTime() {
        return peerDealReqTime;
    }

    /**
     * @Param Long peerDealReqTime to set
     */
    public void setPeerDealReqTime(Long peerDealReqTime) {
        this.peerDealReqTime = peerDealReqTime;
    }

    /**
     * @Return the String name
     */
    public String getName() {
        return name;
    }

    /**
     * @Param String name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @Return the String version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @Param String version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @Return the String desc
     */
    public String getDesc() {
        return desc;
    }

    /**
     * @Param String desc to set
     */
    public void setDesc(String desc) {
        this.desc = desc;
    }

    /**
     * @Return the String status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @Param String status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @Return the SystemInfo sysInfo
     */
    public SystemInfo getSysInfo() {
        return sysInfo;
    }

    /**
     * @Param SystemInfo sysInfo to set
     */
    public void setSysInfo(SystemInfo sysInfo) {
        this.sysInfo = sysInfo;
    }

}

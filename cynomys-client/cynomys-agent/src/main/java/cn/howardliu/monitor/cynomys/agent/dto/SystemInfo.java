/**
 * @Probject Name: netty-wfj-base
 * @Path: com.wfj.netty.monitor.dtoSystemInfo.java
 * @Create By Jack
 * @Create In 2015年8月26日 下午7:00:31
 */
package cn.howardliu.monitor.cynomys.agent.dto;

import cn.howardliu.gear.monitor.core.jvm.JvmStats;
import cn.howardliu.gear.monitor.core.jvm.JvmStats.GarbageCollector;
import cn.howardliu.gear.monitor.core.memory.MemoryUsage;

import java.util.List;

/**
 * @Class Name SystemInfo
 * @Author Jack
 * @Create In 2015年8月26日
 */
public class SystemInfo {

    private long totalMem;

    private long freeMem;

    private long maxMem;

    private String osName;

    private String sysArch;

    private String version;

    private int availableProcessors;

    private long committedVirtualMemorySize;

    private long processCpuTime;

    private double cpuRatio;

    private double systemCpuRatio;

    private long freeSwapSpaceSize;

    private long freePhysicalMemorySize;

    private long totalPhysicalMemorySize;

    private String hostName;

    private List<String> ips;

    private MemoryUsage heapMemoryUsage;

    private MemoryUsage nonHeapMemoryUsage;

    private int threadCount;

    private int peakThreadCount;

    private int daemonThreadCount;

    private long currentThreadCpuTime;

    private long currentThreadUserTime;

    private String vmName;

    private String vmVendor;

    private String vmVersion;

    private String vmArguments;

    private String classPath;

    private String libraryPath;

    private String compliationName;

    private long totalCompliationTime;

    private List<JvmStats.MemoryPool> memPoolInfos;

    private List<GarbageCollector> GCInfos;

    /**
     * @Return the double systemCpuRatio
     */
    public double getSystemCpuRatio() {
        return systemCpuRatio;
    }

    /**
     * @Param double systemCpuRatio to set
     */
    public void setSystemCpuRatio(double systemCpuRatio) {
        this.systemCpuRatio = systemCpuRatio;
    }

    /**
     * @Return the double cpuRatio
     */
    public double getCpuRatio() {
        return cpuRatio;
    }

    /**
     * @Param double cpuRatio to set
     */
    public void setCpuRatio(double cpuRatio) {
        this.cpuRatio = cpuRatio;
    }

    /**
     * @Return the long committedVirtualMemorySize
     */
    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    /**
     * @Param long committedVirtualMemorySize to set
     */
    public void setCommittedVirtualMemorySize(long committedVirtualMemorySize) {
        this.committedVirtualMemorySize = committedVirtualMemorySize;
    }

    /**
     * @Return the long totalMem
     */
    public long getTotalMem() {
        return totalMem;
    }

    /**
     * @Param long totalMem to set
     */
    public void setTotalMem(long totalMem) {
        this.totalMem = totalMem;
    }

    /**
     * @Return the long freeMem
     */
    public long getFreeMem() {
        return freeMem;
    }

    /**
     * @Param long freeMem to set
     */
    public void setFreeMem(long freeMem) {
        this.freeMem = freeMem;
    }

    /**
     * @Return the long maxMem
     */
    public long getMaxMem() {
        return maxMem;
    }

    /**
     * @Param long maxMem to set
     */
    public void setMaxMem(long maxMem) {
        this.maxMem = maxMem;
    }

    /**
     * @Return the String osName
     */
    public String getOsName() {
        return osName;
    }

    /**
     * @Param String osName to set
     */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * @Return the String sysArch
     */
    public String getSysArch() {
        return sysArch;
    }

    /**
     * @Param String sysArch to set
     */
    public void setSysArch(String sysArch) {
        this.sysArch = sysArch;
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
     * @Return the int availableProcessors
     */
    public int getAvailableProcessors() {
        return availableProcessors;
    }

    /**
     * @Param int availableProcessors to set
     */
    public void setAvailableProcessors(int availableProcessors) {
        this.availableProcessors = availableProcessors;
    }

    /**
     * @Return the long processCpuTime
     */
    public long getProcessCpuTime() {
        return processCpuTime;
    }

    /**
     * @Param long processCpuTime to set
     */
    public void setProcessCpuTime(long processCpuTime) {
        this.processCpuTime = processCpuTime;
    }

    /**
     * @Return the long freeSwapSpaceSize
     */
    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    /**
     * @Param long freeSwapSpaceSize to set
     */
    public void setFreeSwapSpaceSize(long freeSwapSpaceSize) {
        this.freeSwapSpaceSize = freeSwapSpaceSize;
    }

    /**
     * @Return the long freePhysicalMemorySize
     */
    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    /**
     * @Param long freePhysicalMemorySize to set
     */
    public void setFreePhysicalMemorySize(long freePhysicalMemorySize) {
        this.freePhysicalMemorySize = freePhysicalMemorySize;
    }

    /**
     * @Return the long totalPhysicalMemorySize
     */
    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    /**
     * @Param long totalPhysicalMemorySize to set
     */
    public void setTotalPhysicalMemorySize(long totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = totalPhysicalMemorySize;
    }

    /**
     * @Return the String hostName
     */
    public String getHostName() {
        return hostName;
    }

    /**
     * @Param String hostName to set
     */
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    /**
     * @Return the List<String> ips
     */
    public List<String> getIps() {
        return ips;
    }

    /**
     * @Param List<String> ips to set
     */
    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    /**
     * @Return the MemoryUsage memUsage
     */
    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    /**
     * @Param MemoryUsage memUsage to set
     */
    public void setHeapMemoryUsage(MemoryUsage heapMemoryUsage) {
        this.heapMemoryUsage = heapMemoryUsage;
    }

    /**
     * @Return the MemoryUsage nonHeapMemoryUsage
     */
    public MemoryUsage getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    /**
     * @Param MemoryUsage nonHeapMemoryUsage to set
     */
    public void setNonHeapMemoryUsage(MemoryUsage nonHeapMemoryUsage) {
        this.nonHeapMemoryUsage = nonHeapMemoryUsage;
    }

    /**
     * @Return the int threadCount
     */
    public int getThreadCount() {
        return threadCount;
    }

    /**
     * @Param int threadCount to set
     */
    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * @Return the int peakThreadCount
     */
    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    /**
     * @Param int peakThreadCount to set
     */
    public void setPeakThreadCount(int peakThreadCount) {
        this.peakThreadCount = peakThreadCount;
    }

    /**
     * @Return the int daemonThreadCount
     */
    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    /**
     * @Param int daemonThreadCount to set
     */
    public void setDaemonThreadCount(int daemonThreadCount) {
        this.daemonThreadCount = daemonThreadCount;
    }

    /**
     * @Return the long currentThreadCpuTime
     */
    public long getCurrentThreadCpuTime() {
        return currentThreadCpuTime;
    }

    /**
     * @Param long currentThreadCpuTime to set
     */
    public void setCurrentThreadCpuTime(long currentThreadCpuTime) {
        this.currentThreadCpuTime = currentThreadCpuTime;
    }

    /**
     * @Return the long currentThreadUserTime
     */
    public long getCurrentThreadUserTime() {
        return currentThreadUserTime;
    }

    /**
     * @Param long currentThreadUserTime to set
     */
    public void setCurrentThreadUserTime(long currentThreadUserTime) {
        this.currentThreadUserTime = currentThreadUserTime;
    }

    /**
     * @Return the String vmName
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * @Param String vmName to set
     */
    public void setVmName(String vmName) {
        this.vmName = vmName;
    }

    /**
     * @Return the String vmVendor
     */
    public String getVmVendor() {
        return vmVendor;
    }

    /**
     * @Param String vmVendor to set
     */
    public void setVmVendor(String vmVendor) {
        this.vmVendor = vmVendor;
    }

    /**
     * @Return the String vmVersion
     */
    public String getVmVersion() {
        return vmVersion;
    }

    /**
     * @Param String vmVersion to set
     */
    public void setVmVersion(String vmVersion) {
        this.vmVersion = vmVersion;
    }

    /**
     * @Return the String classPath
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * @Param String classPath to set
     */
    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    /**
     * @Return the String libraryPath
     */
    public String getLibraryPath() {
        return libraryPath;
    }

    /**
     * @Param String libraryPath to set
     */
    public void setLibraryPath(String libraryPath) {
        this.libraryPath = libraryPath;
    }

    /**
     * @Return the String compliationName
     */
    public String getCompliationName() {
        return compliationName;
    }

    /**
     * @Param String compliationName to set
     */
    public void setCompliationName(String compliationName) {
        this.compliationName = compliationName;
    }

    /**
     * @Return the long totalCompliationTime
     */
    public long getTotalCompliationTime() {
        return totalCompliationTime;
    }

    /**
     * @Param long totalCompliationTime to set
     */
    public void setTotalCompliationTime(long totalCompliationTime) {
        this.totalCompliationTime = totalCompliationTime;
    }

    /**
     * @Return the List<MemPoolInfo> memPoolInfos
     */
    public List<JvmStats.MemoryPool> getMemPoolInfos() {
        return memPoolInfos;
    }

    /**
     * @Param List<MemPoolInfo> memPoolInfos to set
     */
    public void setMemPoolInfos(List<JvmStats.MemoryPool> memPoolInfos) {
        this.memPoolInfos = memPoolInfos;
    }

    /**
     * @Return the List<GCInfo> GCInfos
     */
    public List<GarbageCollector> getGCInfos() {
        return GCInfos;
    }

    /**
     * @Param List<GCInfo> gCInfos to set
     */
    public void setGCInfos(List<GarbageCollector> gCInfos) {
        GCInfos = gCInfos;
    }

    /**
     * @Return the String vmArguments
     */
    public String getVmArguments() {
        return vmArguments;
    }

    /**
     * @Param String vmArguments to set
     */
    public void setVmArguments(String vmArguments) {
        this.vmArguments = vmArguments;
    }


}

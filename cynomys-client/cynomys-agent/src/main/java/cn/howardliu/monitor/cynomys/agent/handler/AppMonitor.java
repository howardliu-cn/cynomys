package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.gear.monitor.core.os.NetworkInterfaceInfo;
import cn.howardliu.gear.monitor.core.os.OsInfo;
import cn.howardliu.monitor.cynomys.agent.counter.SLACounter;
import cn.howardliu.monitor.cynomys.agent.dto.*;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.RequestWrapper;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.howardliu.monitor.cynomys.common.Constant.*;

/**
 * @author Jack
 * @author liuxh
 * @Class Name AppMonitor
 * @Create In 2015年11月10日
 */
public class AppMonitor {
    private static final String TIME_FMT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static AppMonitor appMonitor = null;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Integer port;
    private JavaInformations javaInfor;

    private AppMonitor(Integer p) {
        this.port = p;
        javaInfor = JavaInformations.instance(true);
    }

    /**
     * 获取实例
     *
     * @param p 控制端口
     * @return AppMonitor
     * @Methods Name instance
     * @Create In 2015年11月10日 By Jack
     */
    public static AppMonitor instance(Integer p) {
        if (appMonitor != null) {
            return appMonitor;
        } else {
            appMonitor = new AppMonitor(p);
            return appMonitor;
        }
    }

    /**
     * @Param Integer port to set
     */
    public void setPort(Integer port) {
        this.port = port;
    }

    /**
     * 初始化 ZK监控根节点信息
     *
     * @param status 一般为 Active
     * @return String AppServerPath 监控系统根目录地址
     * @throws InterruptedException
     * @Methods Name buildMonitorRootInfo
     * @Create In 2015年9月7日 By Jack
     */
    public String buildMonitorRootInfo(String status) {
        // 1. 判断用于监控的父节点是否存在，如不存在则建立，每个集成Netty-WFJ-Base的服务均会检查此配置，争抢建立,利用Zookeeper的原生节点创建锁完成
        // TODO 确认ProxyServer是否连通
        boolean isMonitorRootExist = false;
        if (!isMonitorRootExist) {
            Object[] tagArgs = {sysName, sysCode, "Active"};
            String rootDesc = sysDesc;
            rootDesc = MessageFormat.format(rootDesc, tagArgs);
            // TODO 发送初始化状态
            System.out.println(rootDesc);
        }

        // 2. 判断用于监控的系统本身的父节点是否存在，如不存在则建立，建立过程每个此系统的实例争抢创建，利用Zookeeper的原生节点创建锁完成
        String systemDesc = sysName + "-" + sysCode;

        // TODO check this function
        System.out.println(systemDesc);

        // 3. 创建本次实例的临时节点，利用临时节点特性，完成系统监控
        Object[] tagArgs = {sysName, sysCode, status};
        String rootDesc = sysDesc;
        rootDesc = MessageFormat.format(rootDesc, tagArgs);
        // TODO write data
        System.out.println(rootDesc);
        return "";
    }

    /**
     * 获取并构造实例基础信息，并更监控服务器状态信息
     */
    public String buildAppInfo() {
        ApplicationInfo appInfo;
        String rootDesc;
        try {
            DateFormat fmt = new SimpleDateFormat(TIME_FMT_PATTERN);
            SystemInfo sysInfo = new SystemInfo();
            // 0. 判断是否当天计数，如已不是当天，重置计数器，但每个实例目前存在一个5分钟的计数延迟待处理
            compareDate();
            javaInfor.rebuildJavaInfo(true);

            // 1. 获取目前节点基础信息
            Object[] tagArgs = {sysName, sysCode, "Active"};
            rootDesc = sysDesc;
            rootDesc = MessageFormat.format(rootDesc, tagArgs);
            appInfo = JSON.parseObject(rootDesc, ApplicationInfo.class);
            if (appInfo.getDesc() == null) {
                appInfo.setDesc(sysName);
            }

            // 2.开始获取实例基本信息及机器基本信息
            // =======================通过java来获取相关系统状态============================
            sysInfo.setTotalMem(javaInfor.getMemoryInformations().getTotalMemory() / 1024 / 1024);
            sysInfo.setFreeMem(javaInfor.getMemoryInformations().getFreeMemory() / 1024 / 1024);
            sysInfo.setMaxMem(javaInfor.getMemoryInformations().getMaxMemory() / 1024 / 1024);
            // =======================OperatingSystemMXBean============================
            sysInfo.setOsName(javaInfor.getOS());
            sysInfo.setSysArch(javaInfor.getArc());
            sysInfo.setVersion(javaInfor.getSysVersion());
            sysInfo.setAvailableProcessors(javaInfor.getAvailableProcessors());

            sysInfo.setCommittedVirtualMemorySize(javaInfor.getMemoryInformations().getCommittedVirtualMemorySize());

            sysInfo.setProcessCpuTime(javaInfor.getProcessCpuTimeMillis());

            sysInfo.setFreeSwapSpaceSize(javaInfor.getMemoryInformations().getFreeSwapSpaceSize() / 1024 / 1024);
            sysInfo.setFreePhysicalMemorySize(
                    javaInfor.getMemoryInformations().getFreePhysicalMemorySize() / 1024 / 1024);
            sysInfo.setTotalPhysicalMemorySize(
                    javaInfor.getMemoryInformations().getTotalPhysicalMemorySize() / 1024 / 1024);

            sysInfo.setHostName(javaInfor.getHost());
            sysInfo.setIps(getAddress(new OsInfo(), this.port));

            sysInfo.setSystemCpuRatio(javaInfor.getSystemCpuLoad());
            sysInfo.setCpuRatio(javaInfor.getProcessCpuLoad());

            // =======================MemoryMXBean============================
            sysInfo.setHeapMemoryUsage(javaInfor.getMemoryInformations().getHeapMemoryUsage());
            sysInfo.setNonHeapMemoryUsage(javaInfor.getMemoryInformations().getNonHeapMemoryUsage());

            // =======================ThreadMXBean============================
            sysInfo.setThreadCount(javaInfor.getThreadCount());
            sysInfo.setPeakThreadCount(javaInfor.getPeakThreadCount());

            sysInfo.setCurrentThreadCpuTime(javaInfor.getCurrentThreadCpuTime());
            sysInfo.setDaemonThreadCount(javaInfor.getDaemonThreadCount());
            sysInfo.setCurrentThreadUserTime(javaInfor.getCurrentThreadUserTime());

            // =======================CompilationMXBean============================
            // "
            sysInfo.setCompliationName(javaInfor.getCompliationName());
            sysInfo.setTotalCompliationTime(javaInfor.getTotalCompliationTime());
            sysInfo.setMemPoolInfos(javaInfor.getMemoryInformations().getMemPoolInfos());
            sysInfo.setGCInfos(javaInfor.getMemoryInformations().getGcInfos());

            // =======================RuntimeMXBean============================
            // "
            sysInfo.setClassPath(javaInfor.getClassPath());
            sysInfo.setLibraryPath(javaInfor.getLibraryPath());
            sysInfo.setVmName(javaInfor.getVmName());
            sysInfo.setVmVendor(javaInfor.getVmVendor());
            sysInfo.setVmVersion(javaInfor.getVmVersion());
            sysInfo.setVmArguments(javaInfor.getJvmArguments());

            appInfo.setServerTag(cn.howardliu.monitor.cynomys.common.Constant.THIS_TAG);

            // 3.构造实例信息
            appInfo.setSysInfo(sysInfo);
            appInfo.setUpdateTime(fmt.format(new Date()));

            appInfo.setPeerDealReqTime(SLACounter.instance().getPeerDealRequestTime());
            appInfo.setSumInboundReqCounts(SLACounter.instance().getSumInboundRequestCounts());
            appInfo.setSumOutboundReqCounts(SLACounter.instance().getSumOutboundRequestCounts());
            appInfo.setSumDealReqCounts(SLACounter.instance().getSumDealRequestCounts());
            appInfo.setSumDealReqTime(SLACounter.instance().getSumDealRequestTime());
            appInfo.setSumErrDealReqCounts(SLACounter.instance().getSumErrDealRequestCounts());
            appInfo.setSumErrDealReqTime(SLACounter.instance().getSumErrDealRequestTime());

            // 4.更新服务器名称及版本
            if (servletContext == null) {
                appInfo.setServerName(UNKNOWN_SERVER_NAME);
                appInfo.setServerVersion(UNKNOWN_SERVER_VERSION);
            } else {
                String[] svrInfo = servletContext.getServerInfo().split("/");
                appInfo.setServerName(svrInfo[0]);
                appInfo.setServerVersion(svrInfo.length > 1 ? svrInfo[1] : UNKNOWN_SERVER_VERSION);
            }

            appInfo.setTransactionCount(javaInfor.getTransactionCount());
            appInfo.setPid(javaInfor.getPID());
            appInfo.setDataBaseVersion(javaInfor.getDataBaseVersion());
            appInfo.setDataSourceDetails(javaInfor.getDataSourceDetails());

            appInfo.setStartupDate(fmt.format(javaInfor.getStartDate()));
            appInfo.setUnixMaxFileDescriptorCount(javaInfor.getUnixMaxFileDescriptorCount());
            appInfo.setUnixOpenFileDescriptorCount(javaInfor.getUnixOpenFileDescriptorCount());

            // 更新自身节点状态
            return JSON.toJSONString(appInfo);
        } catch (Exception e) {
            log.error("cannot load monitor info: build app info", e);
        }
        return null;
    }

    /**
     * 构造SQL计数信息到队列
     *
     * @Methods Name buildSQLCountsInfo
     * @Create In 2016年5月3日 By Jack
     */
    public String buildSQLCountsInfo() {
        //构造 SQL 信息并发送
        try {
            JdbcWrapper jw = JdbcWrapper.SINGLETON;
            if (jw != null) {
                SQLInfo sqlInfo = SQLInfo.instance();
                sqlInfo.setSysCode(sysCode);
                sqlInfo.setSysName(sysName);
                sqlInfo.setSysIPS(StringUtils.join(getAddress(new OsInfo(), this.port), "<br>"));
                sqlInfo.setDataBaseVersion(javaInfor.getDataBaseVersion());
                sqlInfo.setDataSourceDetails(javaInfor.getDataSourceDetails());
                sqlInfo.setActiveConnectionCount(JdbcWrapper.getActiveConnectionCount());
                sqlInfo.setActiveThreadCount(JdbcWrapper.getActiveThreadCount());
                sqlInfo.setBuildQueueLength(JdbcWrapper.getBuildQueueLength());
                sqlInfo.setRunningBuildCount(JdbcWrapper.getRunningBuildCount());
                sqlInfo.setTransactionCount(JdbcWrapper.getTransactionCount());
                sqlInfo.setUsedConnectionCount(JdbcWrapper.getUsedConnectionCount());
                sqlInfo.setUpdateDate(new SimpleDateFormat(TIME_FMT_PATTERN).format(new Date()));

                List<CounterRequest> sqlDetails = jw.getSqlCounter().getRequests();
                sqlInfo.setSqlDetails(sqlDetails);
                return JSON.toJSONString(sqlInfo);
            }
        } catch (Exception e) {
            log.error("cannot load monitor info: build sql info", e);
        }
        return null;
    }


    public String buildRequestCountInfo() {
        try {
            RequestWrapper rw = RequestWrapper.SINGLETON;
            if (rw != null) {
                RequestInfo reqInfo = RequestInfo.instance();
                reqInfo.setSysCode(sysCode);
                reqInfo.setSysName(sysName);

                reqInfo.setSysIPS(StringUtils.join(getAddress(new OsInfo(), this.port), "<br>"));
                reqInfo.setUpdateDate(new SimpleDateFormat(TIME_FMT_PATTERN).format(new Date()));

                List<CounterRequest> reqDetails = rw.getHttpCounter().getRequests();
                List<CounterRequest> errDetails = rw.getErrorCounter().getRequests();
                reqInfo.setRequestDetails(reqDetails);
                reqInfo.setErrorDetails(errDetails);
                return JSON.toJSONString(reqInfo);
            }
        } catch (Exception e) {
            log.error("cannot load monitor info: build request info", e);
        }
        return null;
    }

    /**
     * @Methods Name compareDate
     * @Create In 2015年9月6日 By Jack void
     */
    private void compareDate() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date cdS = new Date(System.currentTimeMillis());
            Date pdD = SLACounter.instance().getPeerDate();

            Date currentDate = df.parse(df.format(cdS));
            Date startupDate = df.parse(df.format(pdD));

            if (currentDate.compareTo(startupDate) > 0) {
                SLACounter.init();
            }
        } catch (ParseException e) {
            log.error("cannot refresh timer", e);
        }
    }

    private List<String> getAddress(OsInfo osInfo, Integer port) {
        assert osInfo != null && osInfo.getInetAddress() != null && !osInfo.getInetAddress().isEmpty();
        assert port != null && port > 0;

        Set<NetworkInterfaceInfo> inetAddress = osInfo.getInetAddress();
        List<String> addresses = new ArrayList<>(inetAddress.size());
        for (NetworkInterfaceInfo address : inetAddress) {
            Set<String> adds = address.getHostAddresses();
            for (String add : adds) {
                addresses.add(add + ":" + port);
            }
        }
        return addresses;
    }
}

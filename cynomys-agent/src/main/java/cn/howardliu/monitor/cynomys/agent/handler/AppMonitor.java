package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.gear.monitor.core.os.NetworkInterfaceInfo;
import cn.howardliu.gear.monitor.core.os.OsInfo;
import cn.howardliu.monitor.cynomys.agent.conf.Constant;
import cn.howardliu.monitor.cynomys.agent.conf.PropertyAdapter;
import cn.howardliu.monitor.cynomys.agent.conf.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.dto.*;
import cn.howardliu.monitor.cynomys.agent.handler.factory.SLACountManager;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.RequestWrapper;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static cn.howardliu.monitor.cynomys.agent.conf.EnvPropertyConfig.getContextProperty;
import static cn.howardliu.monitor.cynomys.common.Constant.SYS_CODE;
import static cn.howardliu.monitor.cynomys.common.Constant.SYS_DESC;
import static cn.howardliu.monitor.cynomys.common.Constant.SYS_NAME;

/**
 * @author Jack
 * @author liuxh
 * @Class Name AppMonitor
 * @Create In 2015年11月10日
 */
public class AppMonitor {
    private static final String timeFmtPattern = "yyyy-MM-dd HH:mm:ss";
    private static AppMonitor appMonitor = null;
    private final ServletContext sc;
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private Integer port;
    private JavaInformations javaInfor;

    private AppMonitor(Integer p, ServletContext sc) {
        this.port = p;
        this.sc = sc;

        javaInfor = JavaInformations.instance(sc, true);
    }

    /**
     * 获取实例
     *
     * @param p 控制端口
     * @return AppMonitor
     * @Methods Name instance
     * @Create In 2015年11月10日 By Jack
     */
    public static AppMonitor instance(Integer p, ServletContext sc) {
        if (appMonitor != null) {
            return appMonitor;
        } else {
            appMonitor = new AppMonitor(p, sc);
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
    public String buildMonitorRootInfo(String status) throws InterruptedException {
        // 1. 判断用于监控的父节点是否存在，如不存在则建立，每个集成Netty-WFJ-Base的服务均会检查此配置，争抢建立,利用Zookeeper的原生节点创建锁完成
        String rootPath = getContextProperty(
                Constant.SYSTEM_SEETING_SERVER_DEFAULT_SERVER_MONITOR_ROOT_PATH);
        // TODO 确认ProxyServer是否连通
        boolean isMonitorRootExist = false;
        if (!isMonitorRootExist) {
            Object[] tagArgs = {"Active"};
            String rootDesc =
                    getContextProperty(Constant.SYSTEM_SEETING_SERVER_DEFAULT_SERVER_MONITOR_ROOT_DESC);
            rootDesc = PropertyAdapter.formatter(rootDesc, tagArgs);
            // TODO 发送初始化状态
            System.out.println(rootDesc);
        }

        // 2. 判断用于监控的系统本身的父节点是否存在，如不存在则建立，建立过程每个此系统的实例争抢创建，利用Zookeeper的原生节点创建锁完成
        String systemPath = rootPath + "/" + SYS_NAME + "-" + SYS_CODE;

        String systemDesc = SYS_NAME + "-" + SYS_CODE;

        // TODO check this function
        System.out.println(systemPath);
        System.out.println(systemDesc);

        // TODO 获取实例数量，并给出标号
        int instanceID = 0;
        SystemPropertyConfig.setContextProperty(Constant.SYSTEM_SEETING_SERVER_DEFAULT_INSTANCE_KEY,
                SYS_NAME + "-i" + String.valueOf(++instanceID));

        // 3. 创建本次实例的临时节点，利用临时节点特性，完成系统监控
        Object[] tagArgs = {status};
        String rootDesc = SYS_DESC;
        rootDesc = PropertyAdapter.formatter(rootDesc, tagArgs);
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
            DateFormat fmt = new SimpleDateFormat(timeFmtPattern);
            SystemInfo sysInfo = new SystemInfo();
            // 0. 判断是否当天计数，如已不是当天，重置计数器，但每个实例目前存在一个5分钟的计数延迟待处理
            compareDate();
            javaInfor.rebuildJavaInfo(sc, true);

            // 1. 获取目前节点基础信息
            Object[] tagArgs = {"Active"};
            rootDesc = SYS_DESC;
            rootDesc = PropertyAdapter.formatter(rootDesc, tagArgs);
            appInfo = JSON.parseObject(rootDesc, ApplicationInfo.class);

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

            appInfo.setPeerDealReqTime(SLACountManager.instance().getPeerDealRequestTime().longValue());
            appInfo.setSumInboundReqCounts(SLACountManager.instance().getSumInboundRequestCounts().longValue());
            appInfo.setSumOutboundReqCounts(SLACountManager.instance().getSumOutboundRequestCounts().longValue());
            appInfo.setSumDealReqCounts(SLACountManager.instance().getSumDealRequestCounts().longValue());
            appInfo.setSumDealReqTime(SLACountManager.instance().getSumDealRequestTime().longValue());
            appInfo.setSumErrDealReqCounts(SLACountManager.instance().getSumErrDealRequestCounts().longValue());
            appInfo.setSumErrDealReqTime(SLACountManager.instance().getSumErrDealRequestTime().longValue());

            // 4.更新服务器名称及版本
            String svrInfo[] = this.sc.getServerInfo().split(Constant.SYSTEM_SEETING_SERVER_DEFALUT_NAME_VERSION_SPLIT);
            appInfo.setServerName(svrInfo[0]);
            appInfo.setServerVersion(svrInfo.length > 1 ? svrInfo[1] : "unknown");

            appInfo.setTransactionCount(javaInfor.getTransactionCount());
            appInfo.setPid(javaInfor.getPID());
            appInfo.setDataBaseVersion(javaInfor.getDataBaseVersion());
            appInfo.setDataSourceDetails(javaInfor.getDataSourceDetails());

            appInfo.setStartupDate(fmt.format(javaInfor.getStartDate()));
            appInfo.setUnixMaxFileDescriptorCount(javaInfor.getUnixMaxFileDescriptorCount());
            appInfo.setUnixOpenFileDescriptorCount(javaInfor.getUnixOpenFileDescriptorCount());

            appInfo.setDesc(
                    SystemPropertyConfig.getContextProperty(Constant.SYSTEM_SEETING_SERVER_DEFAULT_INSTANCE_KEY));

            // 更新自身节点状态
            // TODO write data
            return JSON.toJSONString(appInfo);
        } catch (Exception e) {
            log.error("cannot load monitor info", e);
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
                sqlInfo.setSysCode(SYS_CODE);
                sqlInfo.setSysName(SYS_NAME);
                sqlInfo.setSysIPS(StringUtils.join(getAddress(new OsInfo(), this.port), "<br>"));
                sqlInfo.setDataBaseVersion(javaInfor.getDataBaseVersion());
                sqlInfo.setDataSourceDetails(javaInfor.getDataSourceDetails());
                sqlInfo.setActive_connection_count(JdbcWrapper.getActiveConnectionCount());
                sqlInfo.setActive_thread_count(JdbcWrapper.getActiveThreadCount());
                sqlInfo.setBuild_queue_length(JdbcWrapper.getBuildQueueLength());
                sqlInfo.setRunning_build_count(JdbcWrapper.getRunningBuildCount());
                sqlInfo.setTransaction_count(JdbcWrapper.getTransactionCount());
                sqlInfo.setUsed_connection_count(JdbcWrapper.getUsedConnectionCount());
                sqlInfo.setUpdateDate(new SimpleDateFormat(timeFmtPattern).format(new Date()));

                List<CounterRequest> sqlDetails = jw.getSqlCounter().getRequests();
                sqlInfo.setSqlDetails(sqlDetails);
                return JSON.toJSONString(sqlInfo);
            }
        } catch (Exception e) {
            log.error("cannot load monitor info", e);
        }
        return null;
    }


    public String buildRequestCountInfo() {
        try {
            RequestWrapper rw = RequestWrapper.SINGLETON;
            if (rw != null) {
                RequestInfo reqInfo = RequestInfo.instance();
                reqInfo.setSysCode(SYS_CODE);
                reqInfo.setSysName(SYS_NAME);

                reqInfo.setSysIPS(StringUtils.join(getAddress(new OsInfo(), this.port), "<br>"));
                reqInfo.setUpdateDate(new SimpleDateFormat(timeFmtPattern).format(new Date()));

                List<CounterRequest> reqDetails = rw.getHttpCounter().getRequests();
                List<CounterRequest> errDetails = rw.getErrorCounter().getRequests();
                reqInfo.setRequestDetails(reqDetails);
                reqInfo.setErrorDetails(errDetails);
                return JSON.toJSONString(reqInfo);
            }
        } catch (Exception e) {
            log.error("cannot load monitor info", e);
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
            Date pdD = SLACountManager.instance().getPeerDate();

            Date currentDate = df.parse(df.format(cdS));
            Date startupDate = df.parse(df.format(pdD));

            if (currentDate.compareTo(startupDate) > 0) {
                SLACountManager.init();
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

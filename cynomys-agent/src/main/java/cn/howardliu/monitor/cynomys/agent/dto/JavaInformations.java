/*
 * Copyright 2008-2016 by Emeric Vernat
 *
 *     This file is part of Java Melody.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.howardliu.monitor.cynomys.agent.dto;

import cn.howardliu.gear.monitor.core.jvm.JvmInfo;
import cn.howardliu.gear.monitor.core.jvm.JvmStats;
import cn.howardliu.gear.monitor.core.jvm.PID;
import cn.howardliu.gear.monitor.core.os.OsInfo;
import cn.howardliu.gear.monitor.core.os.OsStats;
import cn.howardliu.gear.monitor.core.process.ProcessStats;
import cn.howardliu.monitor.cynomys.agent.conf.Parameters;
import cn.howardliu.monitor.cynomys.agent.handler.wrapper.JdbcWrapper;
import cn.howardliu.monitor.cynomys.common.ThreadMXBeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.servlet.ServletContext;
import javax.sql.DataSource;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.*;

import static cn.howardliu.gear.monitor.core.Constants.*;
import static cn.howardliu.monitor.cynomys.common.Constant.SERVLET_CONTEXT;
import static org.apache.commons.lang3.SystemUtils.*;
import static org.apache.commons.lang3.SystemUtils.OS_ARCH;
import static org.apache.commons.lang3.SystemUtils.OS_NAME;
import static org.apache.commons.lang3.SystemUtils.OS_VERSION;

/**
 * Informations systèmes sur le serveur, sans code html de présentation. L'état
 * d'une instance est initialisé à son instanciation et non mutable; il est donc
 * de fait thread-safe. Cet état est celui d'une instance de JVM java, de ses
 * threads et du système à un instant t. Les instances sont sérialisables pour
 * pouvoir être transmises au serveur de collecte.
 *
 * @author Emeric Vernat
 */
public class JavaInformations implements Serializable {
    public static final String os = buildOS();
    private static final Logger log = LoggerFactory.getLogger(JavaInformations.class);
    private static final double HIGH_USAGE_THRESHOLD_IN_PERCENTS = 95d;
    /**
     * 一下计算 CUP 占用率使用
     */
    private static final int CPUTIME = 30;
    private static final int PERCENT = 100;
    private static final int FAULTLENGTH = 10;
    private static final long serialVersionUID = 3281861236369720876L;
    private static final boolean SYSTEM_CPU_LOAD_ENABLED = "1.7".compareTo(SystemUtils.JAVA_VERSION) < 0;

    private static JavaInformations JavaInfo = null;

    private static boolean localWebXmlExists = true;
    private static boolean localPomXmlExists = true;
    private MemoryInformations memoryInformations;
    private List<TomcatInformations> tomcatInformationsList = Collections.synchronizedList(new ArrayList<>());
    private int sessionCount;
    private long sessionAgeSum;
    private int activeThreadCount;
    private int usedConnectionCount;
    private int maxConnectionCount;
    private int activeConnectionCount;
    private long transactionCount;
    private long processCpuTimeMillis;
    private double systemLoadAverage;
    private double systemCpuLoad;
    private double beforeCpuTime = 0;
    private double beforeCpuUpTime = 0;
    private double processCpuLoad;
    private long unixOpenFileDescriptorCount;
    private long unixMaxFileDescriptorCount;
    private String host;
    private String arc;
    private String sysVersion;
    private int availableProcessors;
    private String javaVersion;
    private String jvmVersion;
    private String serverInfo;
    private String contextPath;
    private String contextDisplayName;
    private Date startDate;
    private String jvmArguments;
    private long freeDiskSpaceInTemp;
    private int threadCount;
    private int peakThreadCount;
    private int daemonThreadCount;
    private long totalStartedThreadCount;
    private long currentThreadCpuTime;
    private long currentThreadUserTime;
    private String vmName;
    private String vmVendor;
    private String vmVersion;
    private String classPath;
    private String libraryPath;
    private String compliationName;
    private long totalCompliationTime;
    // 以下部分属于详细信息部分
    private String pid;
    private String dataBaseVersion;
    private String dataSourceDetails;
    @SuppressWarnings("all")
    private List<ThreadInformations> threadInformationsList;
    @SuppressWarnings("all")
    private List<String> dependenciesList;
    private boolean webXmlExists = localWebXmlExists;
    private boolean pomXmlExists = localPomXmlExists;

    private JavaInformations(boolean includeDetails) {
        super();
        buildJavaInfo(includeDetails);
    }

    public static JavaInformations instance(boolean includeDetails) {
        if (JavaInfo != null) {
            return JavaInfo;
        } else {
            JavaInfo = new JavaInformations(includeDetails);
            return JavaInfo;
        }
    }

    public static void setWebXmlExistsAndPomXmlExists(boolean webXmlExists, boolean pomXmlExists) {
        localWebXmlExists = webXmlExists;
        localPomXmlExists = pomXmlExists;
    }

    private static String buildOS() {
        final String name = OS_NAME;
        final String patchLevel = System.getProperty("sun.os.patch.level");
        final String bits = System.getProperty("sun.arch.data.model");

        final StringBuilder sb = new StringBuilder();
        sb.append(name).append(", ");
        if (!name.toLowerCase(Locale.ENGLISH).contains("windows")) {
            // version is "6.1" and useless for os.name "Windows 7",
            // and can be "2.6.32-358.23.2.el6.x86_64" for os.name "Linux"
            sb.append(OS_VERSION).append(' ');
        }
        if (!"unknown".equals(patchLevel)) {
            // patchLevel is "unknown" and useless on Linux,
            // and can be "Service Pack 1" on Windows
            sb.append(patchLevel);
        }
        sb.append(", ").append(OS_ARCH).append('/').append(bits);
        return sb.toString();
    }

    /**
     * 获得CPU使用率.
     *
     * @return 返回cpu使用率
     * @author GuoHuang
     */
    private static double getCpuRatioForWindows() {
        try {
            String procCmd = System
                    .getenv("windir") + "\\system32\\wbem\\wmic.exe process get Caption,CommandLine,KernelModeTime,ReadOperationCount,ThreadCount,UserModeTime,WriteOperationCount";

            // 取进程信息
            double[] c0 = readCpu(Runtime.getRuntime().exec(procCmd));
            Thread.sleep(CPUTIME);
            double[] c1 = readCpu(Runtime.getRuntime().exec(procCmd));
            if (c0 != null && c1 != null) {
                double idletime = c1[0] - c0[0];
                double busytime = c1[1] - c0[1];
                return PERCENT * (busytime) / (busytime + idletime);
            }
        } catch (Exception e) {
            log.error("cannot load monitor info", e);
        }
        return 0;
    }

    /**
     * 读取CPU信息.
     *
     * @param proc
     * @return
     * @author GuoHuang
     */
    private static double[] readCpu(final Process proc) {
        double[] retn = new double[2];
        try {
            proc.getOutputStream().close();
            InputStreamReader ir = new InputStreamReader(proc.getInputStream());
            LineNumberReader input = new LineNumberReader(ir);
            String line = input.readLine();
            if (line == null || line.length() < FAULTLENGTH) {
                return null;
            }
            int capidx = line.indexOf("Caption");
            int cmdidx = line.indexOf("CommandLine");
            int rocidx = line.indexOf("ReadOperationCount");
            int umtidx = line.indexOf("UserModeTime");
            int kmtidx = line.indexOf("KernelModeTime");
            int wocidx = line.indexOf("WriteOperationCount");
            double idletime = 0;
            double kneltime = 0;
            double usertime = 0;
            while ((line = input.readLine()) != null) {
                if (line.length() < wocidx) {
                    continue;
                }
                // 字段出现顺序：Caption,CommandLine,KernelModeTime,ReadOperationCount,
                // ThreadCount,UserModeTime,WriteOperation
                String caption = line.substring(capidx, cmdidx - 1).trim();
                String cmd = line.substring(cmdidx, kmtidx - 1).trim();
                if (cmd.contains("wmic.exe")) {
                    continue;
                }
                String s1 = line.substring(kmtidx, rocidx - 1).trim();
                String s2 = line.substring(umtidx, wocidx - 1).trim();
                if (caption.equals("System Idle Process") || caption.equals("System")) {
                    if (s1.length() > 0) {
                        idletime += Long.valueOf(s1);
                    }
                    if (s2.length() > 0) {
                        idletime += Long.valueOf(s2);
                    }
                    continue;
                }
                if (s1.length() > 0) {
                    kneltime += Long.valueOf(s1);
                }
                if (s2.length() > 0) {
                    usertime += Long.valueOf(s2);
                }
            }
            retn[0] = idletime;
            retn[1] = kneltime + usertime;
            return retn;
        } catch (Exception e) {
            log.error("cannot load monitor info", e);
        } finally {
            try {
                proc.getInputStream().close();
            } catch (Exception e) {
                log.error("cannot load monitor info", e);
            }
        }
        return null;
    }

    private static List<ThreadInformations> buildThreadInformationsList() {
        final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        final Map<Thread, StackTraceElement[]> stackTraces = Thread.getAllStackTraces();
        final List<Thread> threads = new ArrayList<>(stackTraces.keySet());
        final long[] deadlockedThreads = getDeadlockedThreads(threadBean);
        final List<ThreadInformations> threadInfosList = new ArrayList<>(threads.size());
        final String hostAddress = Parameters.getHostAddress();
        for (final Thread thread : threads) {
            final StackTraceElement[] stackTraceElements = stackTraces.get(thread);
            final List<StackTraceElement> stackTraceElementList = stackTraceElements == null ? null : new ArrayList<>(
                    Arrays.asList(stackTraceElements));
            final long cpuTimeMillis = ThreadMXBeanUtils.getThreadCpuTime(thread.getId()) / 1000000;
            final long userTimeMillis = ThreadMXBeanUtils.getThreadUserTime(thread.getId()) / 1000000;
            final boolean deadlocked = deadlockedThreads != null
                    && Arrays.binarySearch(deadlockedThreads, thread.getId()) >= 0;
            threadInfosList.add(new ThreadInformations(thread, stackTraceElementList, cpuTimeMillis, userTimeMillis,
                    deadlocked, hostAddress));
        }
        return threadInfosList;
    }

    private static long[] getDeadlockedThreads(ThreadMXBean threadBean) {
        final long[] deadlockedThreads;
        if (threadBean.isSynchronizerUsageSupported()) {
            deadlockedThreads = threadBean.findDeadlockedThreads();
        } else {
            deadlockedThreads = threadBean.findMonitorDeadlockedThreads();
        }
        if (deadlockedThreads != null) {
            Arrays.sort(deadlockedThreads);
        }
        return deadlockedThreads;
    }

    // TODO build DataBase information when get connection
    private static String buildDataBaseVersion() {
        if (Parameters.isNoDatabase()) {
            return null;
        }
        final StringBuilder result = new StringBuilder();
        try {
            // 我们正在寻找一个datasource与initialcontext显示名称
            // 版本/和BDD +名称和版本的JDBC驱动程序
            //（名称中查找JNDI是datasource属
            // JDBC / XXX是一名datasource）标准图<字符串>，datasource datasources = jdbcwrapp
            final Map<String, DataSource> dataSources = JdbcWrapper.getJndiAndSpringDataSources();
            for (final Map.Entry<String, DataSource> entry : dataSources.entrySet()) {
                final String name = entry.getKey();
                final DataSource dataSource = entry.getValue();
                try (Connection connection = dataSource.getConnection()) {
                    if (result.length() > 0) {
                        result.append("\n\n");
                    }
                    result.append(name).append(":\n");
                    appendDataBaseVersion(result, connection);
                }
            }
        } catch (final Exception e) {
            result.append(e.toString());
        }
        if (result.length() > 0) {
            return result.toString();
        }
        return null;
    }

    private static void appendDataBaseVersion(StringBuilder result, Connection connection) throws SQLException {
        final DatabaseMetaData metaData = connection.getMetaData();
        // Sécurité: pour l'instant on n'indique pas metaData.getUserName()
        result.append(metaData.getURL()).append('\n');
        result.append(metaData.getDatabaseProductName()).append(", ").append(metaData.getDatabaseProductVersion())
                .append('\n');
        result.append("Driver JDBC:\n").append(metaData.getDriverName()).append(", ")
                .append(metaData.getDriverVersion());
    }

    private static String buildDataSourceDetails() {
        final Map<String, Map<String, Object>> dataSourcesProperties = JdbcWrapper.getBasicDataSourceProperties();
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, Map<String, Object>> entry : dataSourcesProperties.entrySet()) {
            final Map<String, Object> dataSourceProperties = entry.getValue();
            if (dataSourceProperties.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append('\n');
            }
            final String name = entry.getKey();
            if (name != null) {
                sb.append(name).append(":\n");
            }
            for (final Map.Entry<String, Object> propertyEntry : dataSourceProperties.entrySet()) {
                sb.append(propertyEntry.getKey()).append(" = ").append(propertyEntry.getValue()).append('\n');
            }
        }
        if (sb.length() == 0) {
            return null;
        }
        return sb.toString();
    }

    private static List<String> buildDependenciesList(ServletContext servletContext) {
        final String directoryTomcat = "/WEB-INF/lib/";
        final String directoryNotTomcat = "/lib/";
        String directory;

        Set<String> dependencies;
        try {
            dependencies = servletContext.getResourcePaths(directoryTomcat);
            directory = directoryTomcat;
        } catch (final Exception e) {
            // Tomcat 8 can throw
            // "IllegalStateException: The resources may not be accessed if they are not currently started"
            // for some ServletContext states (issue 415)
            return Collections.emptyList();
        }
        if (dependencies == null || dependencies.isEmpty()) {
            try {
                dependencies = servletContext.getResourcePaths(directoryNotTomcat);
                directory = directoryNotTomcat;
            } catch (final Exception e) {
                // Tomcat 8 can throw
                // "IllegalStateException: The resources may not be accessed if they are not currently started"
                // for some ServletContext states (issue 415)
                return Collections.emptyList();
            }
        }
        if (dependencies == null || dependencies.isEmpty()) {
            return Collections.emptyList();
        }
        final List<String> result = new ArrayList<>(dependencies.size());
        for (final String dependency : dependencies) {
            result.add(dependency.substring(directory.length()));
        }
        Collections.sort(result);
        return result;
    }

    /**
     * @return the Logger log
     */
    public static Logger getLog() {
        return log;
    }

    /**
     * @return the int CPUTIME
     */
    public static int getCputime() {
        return CPUTIME;
    }

    /**
     * @return the int PERCENT
     */
    public static int getPercent() {
        return PERCENT;
    }

    /**
     * @return the int FAULTLENGTH
     */
    public static int getFaultlength() {
        return FAULTLENGTH;
    }

    /**
     * @return the double HIGH_USAGE_THRESHOLD_IN_PERCENTS
     */
    public static double getHighUsageThresholdInPercents() {
        return HIGH_USAGE_THRESHOLD_IN_PERCENTS;
    }

    /**
     * @return the long serialVersionUID
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * @return the boolean SYSTEM_CPU_LOAD_ENABLED
     */
    public static boolean isSystemCpuLoadEnabled() {
        return SYSTEM_CPU_LOAD_ENABLED;
    }

    /**
     * @return the boolean localWebXmlExists
     */
    public static boolean isLocalWebXmlExists() {
        return localWebXmlExists;
    }

    /**
     * @return the boolean localPomXmlExists
     */
    public static boolean isLocalPomXmlExists() {
        return localPomXmlExists;
    }

    /**
     * @return the JavaInformations JavaInfo
     */
    public static JavaInformations getJavaInfo() {
        return JavaInfo;
    }

    /**
     * 重建监控信息
     */
    public void rebuildJavaInfo(boolean includeDetails) {
        clearJavaInfo();
        buildJavaInfo(includeDetails);
    }

    /**
     * 清理基本信息
     *
     * @Methods Name clearJavaInfo
     * @Create In 2016年3月26日 By Jack void
     */
    public void clearJavaInfo() {
        // 链接信息
        sessionCount = 0;// SessionListener.getSessionCount();
        sessionAgeSum = 0; // SessionListener.getSessionAgeSum();

        // 数据源信息
        activeThreadCount = 0;
        usedConnectionCount = 0;
        activeConnectionCount = 0;
        maxConnectionCount = 0;
        transactionCount = 0;

        // 系统信息
        memoryInformations = null;
        tomcatInformationsList.clear();
        systemLoadAverage = 0;
        systemCpuLoad = 0;
        processCpuTimeMillis = 0;
        unixOpenFileDescriptorCount = 0;
        unixMaxFileDescriptorCount = 0;
        host = "";
        availableProcessors = 0;
        javaVersion = "";
        jvmVersion = "";

        serverInfo = null;
        contextPath = null;
        contextDisplayName = null;
        dependenciesList = null;

        jvmArguments = "";
        threadCount = 0;
        peakThreadCount = 0;
        totalStartedThreadCount = 0;
        freeDiskSpaceInTemp = 0;

        dataBaseVersion = null;
        dataSourceDetails = null;
        threadInformationsList = null;
        pid = null;

        arc = "";
        sysVersion = "";
        processCpuLoad = 0;
        daemonThreadCount = 0;
        currentThreadCpuTime = 0;
        currentThreadUserTime = 0;

        vmName = "";
        vmVendor = "";
        vmVersion = "";

        classPath = "";
        libraryPath = "";
        compliationName = "";
        totalCompliationTime = 0;
    }

    private void buildJavaInfo(boolean includeDetails) {
        JvmStats jvmStats = JvmStats.stats();
        OsStats osStats = OsStats.stats();
        ProcessStats processStats = ProcessStats.stats();
        JvmInfo jvmInfo = JvmInfo.instance();
        OsInfo osInfo = new OsInfo();

        // 链接信息
        sessionCount = 0;// SessionListener.getSessionCount();
        sessionAgeSum = 0; // SessionListener.getSessionAgeSum();

        // 数据源信息
        activeThreadCount = JdbcWrapper.getActiveThreadCount();
        usedConnectionCount = JdbcWrapper.getUsedConnectionCount();
        activeConnectionCount = JdbcWrapper.getActiveConnectionCount();
        maxConnectionCount = JdbcWrapper.getMaxConnectionCount();
        transactionCount = JdbcWrapper.getTransactionCount();

        // 系统信息
        memoryInformations = new MemoryInformations(jvmStats, osStats, processStats);

        tomcatInformationsList = TomcatInformations.buildTomcatInformationsList();

        double[] loadAverages = osStats.getCpu().getLoadAverages();
        if (loadAverages == null || loadAverages.length == 0) {
            systemLoadAverage = 0;
        } else {
            systemLoadAverage = loadAverages[0];
        }

        systemCpuLoad = osStats.getCpu().getPercent();
        processCpuTimeMillis = processStats.getCpu().getTotalTime();
        unixOpenFileDescriptorCount = processStats.getOpenFileDescriptorCount();
        unixMaxFileDescriptorCount = processStats.getMaxFileDescriptorCount();
        host = Parameters.getHostName() + '@' + Parameters.getHostAddress();

        arc = osInfo.getArch();
        sysVersion = osInfo.getVersion();
        processCpuLoad = processStats.getCpu().getPercent();

        availableProcessors = osStats.getAvailableProcessors();
        javaVersion = JAVA_RUNTIME_NAME + ", " + JAVA_RUNTIME_VERSION;
        jvmVersion = JAVA_VM_NAME + ", " + JAVA_VM_VERSION + ", " + JAVA_VM_INFO;

        vmName = JVM_NAME;
        vmVendor = JVM_VENDOR;
        vmVersion = JVM_VERSION;

        classPath = jvmInfo.getRuntimeInfo().getClassPath();
        libraryPath = jvmInfo.getRuntimeInfo().getLibraryPath();
        compliationName = jvmStats.getCompilationInfo().getName();
        totalCompliationTime = jvmStats.getCompilationInfo().getCompilationTime();

        if (SERVLET_CONTEXT == null) {
            serverInfo = null;
            contextPath = null;
            contextDisplayName = null;
            dependenciesList = null;
        } else {
            serverInfo = SERVLET_CONTEXT.getServerInfo();
            contextPath = Parameters.getContextPath(SERVLET_CONTEXT);
            contextDisplayName = SERVLET_CONTEXT.getServletContextName();
            dependenciesList = buildDependenciesList(SERVLET_CONTEXT);
        }

        startDate = START_TIME;
        jvmArguments = StringUtils.join(jvmInfo.getRuntimeInfo().getInputArguments(), '\n');

        threadCount = jvmStats.getThreads().getCount();
        peakThreadCount = jvmStats.getThreads().getPeakCount();
        totalStartedThreadCount = jvmStats.getThreads().getTotalStartedCount();
        daemonThreadCount = jvmStats.getThreads().getDaemonCount();
        currentThreadCpuTime = jvmStats.getThreads().getCurrentCpuTime();
        currentThreadUserTime = jvmStats.getThreads().getCurrentUserTime();

        // TODO java.io.tmpdir free space
        freeDiskSpaceInTemp = new File(SystemUtils.JAVA_IO_TMPDIR).getFreeSpace();

        if (includeDetails) {
            dataBaseVersion = buildDataBaseVersion();
            dataSourceDetails = buildDataSourceDetails();
            threadInformationsList = buildThreadInformationsList();
            pid = PID.getPID().toString();
        } else {
            dataBaseVersion = null;
            dataSourceDetails = null;
            threadInformationsList = null;
            pid = null;
        }
    }

    public boolean doesWebXmlExists() {
        return webXmlExists;
    }

    public boolean doesPomXmlExists() {
        return pomXmlExists;
    }

    private double getCpuRatioForWindowsByPID() {
        MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();
        com.sun.management.OperatingSystemMXBean osm;
        double cpuUsage = 0;
        try {
            osm = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME,
                    com.sun.management.OperatingSystemMXBean.class);
            if (this.beforeCpuTime == 0) {
                this.beforeCpuTime = osm.getProcessCpuTime();
                this.beforeCpuUpTime = System.nanoTime();
            } else {
                if (osm.getProcessCpuTime() > this.beforeCpuTime) {
                    cpuUsage = ((osm.getProcessCpuTime() - this.beforeCpuTime) * 100L) / (System
                            .nanoTime() - this.beforeCpuUpTime);
                    cpuUsage = Math.abs(cpuUsage);
                    this.beforeCpuTime = osm.getProcessCpuTime();
                    this.beforeCpuUpTime = System.nanoTime();
                } else {
                    this.beforeCpuTime = osm.getProcessCpuTime();
                    this.beforeCpuUpTime = System.nanoTime();
                }
            }
        } catch (IOException e) {
            log.error("cannot load monitor info", e);
        }
        return cpuUsage;
    }

    public MemoryInformations getMemoryInformations() {
        return memoryInformations;
    }

    public List<TomcatInformations> getTomcatInformationsList() {
        return tomcatInformationsList;
    }

    public int getSessionCount() {
        return sessionCount;
    }

    public long getSessionAgeSum() {
        return sessionAgeSum;
    }

    public long getSessionMeanAgeInMinutes() {
        if (sessionCount > 0) {
            return sessionAgeSum / sessionCount / 60000;
        }
        return -1;
    }

    public int getActiveThreadCount() {
        return activeThreadCount;
    }

    public int getUsedConnectionCount() {
        return usedConnectionCount;
    }

    public int getActiveConnectionCount() {
        return activeConnectionCount;
    }

    public int getMaxConnectionCount() {
        return maxConnectionCount;
    }

    public long getTransactionCount() {
        return transactionCount;
    }

    public double getUsedConnectionPercentage() {
        if (maxConnectionCount > 0) {
            return 100d * usedConnectionCount / maxConnectionCount;
        }
        return -1d;
    }

    public long getProcessCpuTimeMillis() {
        return processCpuTimeMillis;
    }

    public double getSystemLoadAverage() {
        return systemLoadAverage;
    }

    public double getSystemCpuLoad() {
        return systemCpuLoad;
    }

    public long getUnixOpenFileDescriptorCount() {
        return unixOpenFileDescriptorCount;
    }

    public long getUnixMaxFileDescriptorCount() {
        return unixMaxFileDescriptorCount;
    }

    public double getUnixOpenFileDescriptorPercentage() {
        if (unixOpenFileDescriptorCount >= 0) {
            return 100d * unixOpenFileDescriptorCount / unixMaxFileDescriptorCount;
        }
        return -1d;
    }

    public String getHost() {
        return host;
    }

    public String getOS() {
        return os;
    }

    public int getAvailableProcessors() {
        return availableProcessors;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public String getJvmVersion() {
        return jvmVersion;
    }

    public String getPID() {
        return pid;
    }

    public String getServerInfo() {
        return serverInfo;
    }

    public String getContextPath() {
        return contextPath;
    }

    public String getContextDisplayName() {
        return contextDisplayName;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getJvmArguments() {
        return jvmArguments;
    }

    public long getFreeDiskSpaceInTemp() {
        return freeDiskSpaceInTemp;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public int getPeakThreadCount() {
        return peakThreadCount;
    }

    public long getTotalStartedThreadCount() {
        return totalStartedThreadCount;
    }

    public String getDataBaseVersion() {
        return dataBaseVersion;
    }

    public String getDataSourceDetails() {
        return dataSourceDetails;
    }

    public List<ThreadInformations> getThreadInformationsList() {
        // on trie sur demande (si affichage)
        final List<ThreadInformations> result = new ArrayList<>(threadInformationsList);
        Collections.sort(result, new ThreadInformationsComparator());
        return Collections.unmodifiableList(result);
    }

    public boolean isDependenciesEnabled() {
        return dependenciesList != null && !dependenciesList.isEmpty();
    }

    public List<String> getDependenciesList() {
        if (dependenciesList != null) {
            return Collections.unmodifiableList(dependenciesList);
        }
        return Collections.emptyList();
    }

    public String getDependencies() {
        if (!isDependenciesEnabled()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        for (final String dependency : getDependenciesList()) {
            if (dependency.endsWith(".jar") || dependency.endsWith(".JAR")) {
                sb.append(dependency);
                sb.append(",\n");
            }
        }
        if (sb.length() >= 2) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.toString();
    }

    public boolean isStackTraceEnabled() {
        for (final ThreadInformations threadInformations : threadInformationsList) {
            final List<StackTraceElement> stackTrace = threadInformations.getStackTrace();
            if (stackTrace != null && !stackTrace.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return the double processCpuLoad
     */
    public double getProcessCpuLoad() {
        return processCpuLoad;
    }

    /**
     * @return the String sysVersion
     */
    public String getSysVersion() {
        return sysVersion;
    }

    /**
     * @return the String arc
     */
    public String getArc() {
        return arc;
    }

    /**
     * @return the int daemonThreadCount
     */
    public int getDaemonThreadCount() {
        return daemonThreadCount;
    }

    /**
     * @return the long currentThreadCpuTime
     */
    public long getCurrentThreadCpuTime() {
        return currentThreadCpuTime;
    }

    /**
     * @return the long currentThreadUserTime
     */
    public long getCurrentThreadUserTime() {
        return currentThreadUserTime;
    }

    /**
     * @return the String vmName
     */
    public String getVmName() {
        return vmName;
    }

    /**
     * @return the String vmVendor
     */
    public String getVmVendor() {
        return vmVendor;
    }

    /**
     * @return the String vmVersion
     */
    public String getVmVersion() {
        return vmVersion;
    }

    /**
     * @return the String classPath
     */
    public String getClassPath() {
        return classPath;
    }

    /**
     * @return the String libraryPath
     */
    public String getLibraryPath() {
        return libraryPath;
    }

    /**
     * @return the String compliationName
     */
    public String getCompliationName() {
        return compliationName;
    }

    /**
     * @return the double beforeCpuTime
     */
    public double getBeforeCpuTime() {
        return beforeCpuTime;
    }

    /**
     * @return the double beforeCpuUpTime
     */
    public double getBeforeCpuUpTime() {
        return beforeCpuUpTime;
    }

    /**
     * @return the String os
     */
    public String getOs() {
        return os;
    }

    /**
     * @return the long totalCompliationTime
     */
    public long getTotalCompliationTime() {
        return totalCompliationTime;
    }

    /**
     * @return the String pid
     */
    public String getPid() {
        return pid;
    }

    /**
     * @return the boolean webXmlExists
     */
    public boolean isWebXmlExists() {
        return webXmlExists;
    }

    /**
     * @return the boolean pomXmlExists
     */
    public boolean isPomXmlExists() {
        return pomXmlExists;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass()
                .getSimpleName() + "[pid=" + getPID() + ", host=" + getHost() + ", javaVersion=" + getJavaVersion() + ", serverInfo=" + getServerInfo() + ']';
    }

    public static final class ThreadInformationsComparator implements Comparator<ThreadInformations>, Serializable {
        private static final long serialVersionUID = 1L;

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(ThreadInformations thread1, ThreadInformations thread2) {
            return thread1.getName().compareToIgnoreCase(thread2.getName());
        }
    }
}

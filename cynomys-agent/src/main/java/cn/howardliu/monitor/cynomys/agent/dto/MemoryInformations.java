package cn.howardliu.monitor.cynomys.agent.dto;

import cn.howardliu.gear.monitor.jvm.JvmStats;
import cn.howardliu.gear.monitor.jvm.JvmStats.GarbageCollector;
import cn.howardliu.gear.monitor.jvm.JvmStats.MemoryPool;
import cn.howardliu.gear.monitor.memory.MemoryUsage;
import cn.howardliu.gear.monitor.os.OsStats;
import cn.howardliu.gear.monitor.process.ProcessStats;

import java.io.Serializable;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class MemoryInformations implements Serializable {
    private static final long serialVersionUID = 3281861236369720876L;
    private static final String NEXT = ",\n";
    private static final String MO = " Mo";

    private final long totalMemory;
    private final long freeMemory;
    private final long usedMemory;
    private final long maxMemory;
    private final long usedPermGen;
    private final long maxPermGen;
    private final long usedNonHeapMemory;
    private final long usedBufferedMemory;
    private final int loadedClassesCount;
    private final long garbageCollectionTimeMillis;

    private final long usedPhysicalMemorySize;
    private final long usedSwapSpaceSize;
    private final long totalSwapSpaceSize;
    private final long freeSwapSpaceSize;
    private final long freePhysicalMemorySize;
    private final long totalPhysicalMemorySize;
    private final long committedVirtualMemorySize;

    private final MemoryUsage heapMemoryUsage;
    private final MemoryUsage nonHeapMemoryUsage;
    private final List<MemoryPool> memPoolInfos;
    private final List<GarbageCollector> gcInfos;

    private final String memoryDetails;

    public MemoryInformations(JvmStats jvmStats, OsStats osStats, ProcessStats processStats) {
        totalMemory = jvmStats.getMem().getJvmMemoryInfo().getTotalMemory().getSize();
        freeMemory = jvmStats.getMem().getJvmMemoryInfo().getFreeMemory().getSize();
        usedMemory = jvmStats.getMem().getJvmMemoryInfo().getUsedMemory().getSize();
        maxMemory = jvmStats.getMem().getJvmMemoryInfo().getMaxMemory().getSize();

        heapMemoryUsage = jvmStats.getMem().getHeapMemoryUsage();
        nonHeapMemoryUsage = jvmStats.getMem().getNonHeapMemoryUsage();
        memPoolInfos = jvmStats.getMem().getPools();
        gcInfos = jvmStats.getGc();

        MemoryUsage permGenMemoryUsage = jvmStats.getMem().getPermGenMemoryUsage();
        usedPermGen = permGenMemoryUsage.getUsed();
        maxPermGen = permGenMemoryUsage.getMax();

        usedNonHeapMemory = jvmStats.getMem().getNonHeapMemoryUsage().getUsed();
        usedBufferedMemory = getUsedBufferMemory(jvmStats.getBufferPools());
        loadedClassesCount = jvmStats.getClasses().getLoadedClassCount();
        garbageCollectionTimeMillis = buildGarbageCollectionTimeMillis(jvmStats.getGc());

        committedVirtualMemorySize = processStats.getMem().getTotalVirtual();

        freeSwapSpaceSize = osStats.getSwap().getFree();
        usedSwapSpaceSize = osStats.getSwap().getUsed();
        totalSwapSpaceSize = osStats.getSwap().getTotal();
        freePhysicalMemorySize = osStats.getMem().getFree();
        usedPhysicalMemorySize = osStats.getMem().getUsed();
        totalPhysicalMemorySize = osStats.getMem().getTotal();

        memoryDetails = buildMemoryDetails();
    }

    public static long getLongFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {
        return (Long) getFromOperatingSystem(operatingSystem, methodName);
    }

    public static double getDoubleFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {
        return (Double) getFromOperatingSystem(operatingSystem, methodName);
    }

    public static Object getFromOperatingSystem(OperatingSystemMXBean operatingSystem, String methodName) {
        try {
            final Method method = operatingSystem.getClass().getMethod(methodName, (Class<?>[]) null);
            method.setAccessible(true);
            return method.invoke(operatingSystem, (Object[]) null);
        } catch (final InvocationTargetException e) {
            if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            } else if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw new IllegalStateException(e.getCause());
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (final IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

    private long getUsedBufferMemory(List<JvmStats.BufferPool> bufferPools) {
        if (bufferPools.isEmpty()) {
            return -1;
        }
        long result = 0;
        for (JvmStats.BufferPool bufferPool : bufferPools) {
            result += bufferPool.getUsed().getSize();
        }
        return result;
    }

    private long buildGarbageCollectionTimeMillis(List<GarbageCollector> gcs) {
        long garbageCollectionTime = 0;
        for (GarbageCollector gc : gcs) {
            garbageCollectionTime += gc.getCollectionTime().getDuration();
        }
        return garbageCollectionTime;
    }

    private String buildMemoryDetails() {
        DecimalFormatSymbols decimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.getDefault());
        final DecimalFormat integerFormat = new DecimalFormat("#,##0", decimalFormatSymbols);
        final String nonHeapMemory = "Non heap memory = " + integerFormat
                .format(usedNonHeapMemory / 1024 / 1024) + MO + " (Perm Gen, Code Cache)";
        // classes actuellement chargées
        final String classLoading = "Loaded classes = " + integerFormat.format(loadedClassesCount);
        final String gc = "Garbage collection time = " + integerFormat.format(garbageCollectionTimeMillis) + " ms";
        final OperatingSystemMXBean operatingSystem = ManagementFactory.getOperatingSystemMXBean();
        String osInfo = "";
        if (isSunOsMBean(operatingSystem)) {
            osInfo = "Process cpu time = " + integerFormat.format(getLongFromOperatingSystem(operatingSystem,
                    "getProcessCpuTime") / 1000000) + " ms,\nCommitted virtual memory = "
                    + integerFormat.format(getLongFromOperatingSystem(operatingSystem,
                    "getCommittedVirtualMemorySize") / 1024 / 1024) + MO + ",\nFree physical memory = "
                    + integerFormat.format(getLongFromOperatingSystem(operatingSystem,
                    "getFreePhysicalMemorySize") / 1024 / 1024) + MO + ",\nTotal physical memory = "
                    + integerFormat.format(getLongFromOperatingSystem(operatingSystem,
                    "getTotalPhysicalMemorySize") / 1024 / 1024) + MO + ",\nFree swap space = "
                    + integerFormat.format(getLongFromOperatingSystem(operatingSystem,
                    "getFreeSwapSpaceSize") / 1024 / 1024) + MO + ",\nTotal swap space = "
                    + integerFormat
                    .format(getLongFromOperatingSystem(operatingSystem, "getTotalSwapSpaceSize") / 1024 / 1024) + MO;
        }
        if (usedBufferedMemory < 0) {
            return nonHeapMemory + NEXT + classLoading + NEXT + gc + NEXT + osInfo;
        }
        final String bufferedMemory = "Buffered memory = " + integerFormat
                .format(usedBufferedMemory / 1024 / 1024) + MO;
        return nonHeapMemory + NEXT + bufferedMemory + NEXT + classLoading + NEXT + gc + NEXT + osInfo;
    }

    private boolean isSunOsMBean(OperatingSystemMXBean operatingSystem) {
        // on ne teste pas operatingSystem instanceof
        // com.sun.management.OperatingSystemMXBean
        // car le package com.sun n'existe à priori pas sur une jvm tierce
        final String className = operatingSystem.getClass().getName();
        return "com.sun.management.OperatingSystem".equals(className) || "com.sun.management.UnixOperatingSystem"
                .equals(className)
                // sun.management.OperatingSystemImpl pour java 8
                || "sun.management.OperatingSystemImpl".equals(className);
    }

    public long getUsedMemory() {
        return usedMemory;
    }

    public long getMaxMemory() {
        return maxMemory;
    }

    public double getUsedMemoryPercentage() {
        return 100d * usedMemory / maxMemory;
    }

    public long getUsedPermGen() {
        return usedPermGen;
    }

    public long getMaxPermGen() {
        return maxPermGen;
    }

    public double getUsedPermGenPercentage() {
        if (usedPermGen > 0 && maxPermGen > 0) {
            return 100d * usedPermGen / maxPermGen;
        }
        return -1d;
    }

    public long getUsedNonHeapMemory() {
        return usedNonHeapMemory;
    }

    public long getUsedBufferedMemory() {
        return usedBufferedMemory;
    }

    public int getLoadedClassesCount() {
        return loadedClassesCount;
    }

    public long getGarbageCollectionTimeMillis() {
        return garbageCollectionTimeMillis;
    }

    public long getUsedPhysicalMemorySize() {
        return usedPhysicalMemorySize;
    }

    public long getUsedSwapSpaceSize() {
        return usedSwapSpaceSize;
    }

    public String getMemoryDetails() {
        return memoryDetails;
    }

    /**
     * @return the long totalMemory
     */
    public long getTotalMemory() {
        return totalMemory;
    }

    /**
     * @return the long freeMemory
     */
    public long getFreeMemory() {
        return freeMemory;
    }


    /**
     * @return the long totalSwapSpaceSize
     */
    public long getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    /**
     * @return the long freeSwapSpaceSize
     */
    public long getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    /**
     * @return the long freePhysicalMemorySize
     */
    public long getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    /**
     * @return the long totalPhysicalMemorySize
     */
    public long getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    /**
     * @return the long committedVirtualMemorySize
     */
    public long getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }


    /**
     * @return the MemoryUsage heapMemoryUsage
     */
    public MemoryUsage getHeapMemoryUsage() {
        return heapMemoryUsage;
    }

    /**
     * @return the MemoryUsage nonHeapMemoryUsage
     */
    public MemoryUsage getNonHeapMemoryUsage() {
        return nonHeapMemoryUsage;
    }

    /**
     * @return the List<MemPoolInfo> memPoolInfos
     */
    public List<MemoryPool> getMemPoolInfos() {
        return memPoolInfos;
    }

    /**
     * @return the List<GCInfo> gcInfos
     */
    public List<GarbageCollector> getGcInfos() {
        return gcInfos;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[usedMemory=" + getUsedMemory() + ", maxMemory=" + getMaxMemory() + ']';
    }
}

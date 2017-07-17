/**
 * @Probject Name: netty-wfj-base
 * @Path: com.wfj.netty.monitor.dtoMemPoolInfo.java
 * @Create By Jack
 * @Create In 2015年8月26日 下午9:13:24
 */
package cn.howardliu.monitor.cynomys.agent.dto;


/**
 * @Class Name MemPoolInfo
 * @Author Jack
 * @Create In 2015年8月26日
 */
public class MemPoolInfo {

    private String memoryManagerNames;

    private MemoryUsage memoryUsage;

    /**
     * @Return the String memoryManagerNames
     */
    public String getMemoryManagerNames() {
        return memoryManagerNames;
    }

    /**
     * @Param String memoryManagerNames to set
     */
    public void setMemoryManagerNames(String memoryManagerNames) {
        this.memoryManagerNames = memoryManagerNames;
    }

    /**
     * @Return the MemoryUsage memoryUsage
     */
    public MemoryUsage getMemoryUsage() {
        return memoryUsage;
    }

    /**
     * @Param MemoryUsage memoryUsage to set
     */
    public void setMemoryUsage(MemoryUsage memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

}

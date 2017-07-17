/**
 * @Probject Name: netty-wfj-base
 * @Path: com.wfj.netty.monitor.dtoGCInfo.java
 * @Create By Jack
 * @Create In 2015年8月26日 下午9:15:42
 */
package cn.howardliu.monitor.cynomys.agent.dto;

/**
 * @Class Name GCInfo
 * @Author Jack
 * @Create In 2015年8月26日
 */
public class GCInfo {

    private String name;

    private long collectionCount;

    private long collectionTime;

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
     * @Return the long collectionCount
     */
    public long getCollectionCount() {
        return collectionCount;
    }

    /**
     * @Param long collectionCount to set
     */
    public void setCollectionCount(long collectionCount) {
        this.collectionCount = collectionCount;
    }

    /**
     * @Return the long collectionTime
     */
    public long getCollectionTime() {
        return collectionTime;
    }

    /**
     * @Param long collectionTime to set
     */
    public void setCollectionTime(long collectionTime) {
        this.collectionTime = collectionTime;
    }
}

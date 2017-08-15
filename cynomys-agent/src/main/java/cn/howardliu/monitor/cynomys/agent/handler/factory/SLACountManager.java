/**
 * @Probject Name: netty-wfj-monitor
 * @Path: com.wfj.netty.monitor.handlerSLACountManager.java
 * @Create By Jack
 * @Create In 2015年9月6日 下午4:36:39
 */
package cn.howardliu.monitor.cynomys.agent.handler.factory;

import cn.howardliu.monitor.cynomys.agent.common.Constant;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Class Name SLACountManager
 * @Author Jack
 * @Create In 2015年9月6日
 */
// TODO replace this class
public class SLACountManager {

    private static SLACountManager slam = null;

    private AtomicLong sumInboundRequestCounts;

    private AtomicLong sumOutboundRequestCounts;

    private AtomicLong sumDealRequestCounts;

    private AtomicLong sumErrDealRequestCounts;

    private AtomicLong sumErrDealRequestTime;

    private AtomicLong sumDealRequestTime;

    private AtomicBoolean isDebug;

    private AtomicLong peerDealRequestTime;

    private Date peerDate;


    public static void init() {
        if (slam == null) {
            slam = new SLACountManager();
        }
        slam.setIsDebug(new AtomicBoolean(Constant.IS_DEBUG));
        slam.setSumDealRequestCounts(new AtomicLong());
        slam.setSumDealRequestTime(new AtomicLong());
        slam.setSumInboundRequestCounts(new AtomicLong());
        slam.setSumOutboundRequestCounts(new AtomicLong());
        slam.setPeerDealRequestTime(new AtomicLong());
        slam.setSumErrDealRequestCounts(new AtomicLong());
        slam.setSumErrDealRequestTime(new AtomicLong());
        slam.setPeerDate(new Date());
    }

    public static SLACountManager instance() {
        if (slam == null) {
            init();
            return slam;
        } else {
            return slam;
        }
    }


    /**
     * @Return the AtomicLong sumErrDealRequestCounts
     */
    public AtomicLong getSumErrDealRequestCounts() {
        return sumErrDealRequestCounts;
    }

    /**
     * @Param AtomicLong sumErrDealRequestCounts to set
     */
    public void setSumErrDealRequestCounts(AtomicLong sumErrDealRequestCounts) {
        this.sumErrDealRequestCounts = sumErrDealRequestCounts;
    }

    /**
     * @Return the AtomicLong sumErrDealRequestTime
     */
    public AtomicLong getSumErrDealRequestTime() {
        return sumErrDealRequestTime;
    }

    /**
     * @Param AtomicLong sumErrDealRequestTime to set
     */
    public void setSumErrDealRequestTime(AtomicLong sumErrDealRequestTime) {
        this.sumErrDealRequestTime = sumErrDealRequestTime;
    }

    /**
     * @Return the AtomicLong sumInboundRequestCounts
     */
    public AtomicLong getSumInboundRequestCounts() {
        return sumInboundRequestCounts;
    }

    /**
     * @Param AtomicLong sumInboundRequestCounts to set
     */
    public void setSumInboundRequestCounts(AtomicLong sumInboundRequestCounts) {
        this.sumInboundRequestCounts = sumInboundRequestCounts;
    }

    /**
     * @Return the AtomicLong sumOutboundRequestCounts
     */
    public AtomicLong getSumOutboundRequestCounts() {
        return sumOutboundRequestCounts;
    }

    /**
     * @Param AtomicLong sumOutboundRequestCounts to set
     */
    public void setSumOutboundRequestCounts(AtomicLong sumOutboundRequestCounts) {
        this.sumOutboundRequestCounts = sumOutboundRequestCounts;
    }

    /**
     * @Return the AtomicLong sumDealRequestCounts
     */
    public AtomicLong getSumDealRequestCounts() {
        return sumDealRequestCounts;
    }

    /**
     * @Param AtomicLong sumDealRequestCounts to set
     */
    public void setSumDealRequestCounts(AtomicLong sumDealRequestCounts) {
        this.sumDealRequestCounts = sumDealRequestCounts;
    }

    /**
     * @Return the AtomicLong sumDealRequestTime
     */
    public AtomicLong getSumDealRequestTime() {
        return sumDealRequestTime;
    }

    /**
     * @Param AtomicLong sumDealRequestTime to set
     */
    public void setSumDealRequestTime(AtomicLong sumDealRequestTime) {
        this.sumDealRequestTime = sumDealRequestTime;
    }

    /**
     * @Return the AtomicBoolean isDebug
     */
    public AtomicBoolean getIsDebug() {
        return isDebug;
    }

    /**
     * @Param AtomicBoolean isDebug to set
     */
    public void setIsDebug(AtomicBoolean isDebug) {
        this.isDebug = isDebug;
    }

    /**
     * @Return the AtomicLong peerDealRequestTime
     */
    public AtomicLong getPeerDealRequestTime() {
        return peerDealRequestTime;
    }

    /**
     * @Param AtomicLong peerDealRequestTime to set
     */
    public void setPeerDealRequestTime(AtomicLong peerDealRequestTime) {
        this.peerDealRequestTime = peerDealRequestTime;
    }

    /**
     * @Return the Date peerDate
     */
    public Date getPeerDate() {
        return peerDate;
    }

    /**
     * @Param Date peerDate to set
     */
    public void setPeerDate(Date peerDate) {
        this.peerDate = peerDate;
    }


}

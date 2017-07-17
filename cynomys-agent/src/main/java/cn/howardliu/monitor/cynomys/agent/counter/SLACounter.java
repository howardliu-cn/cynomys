package cn.howardliu.monitor.cynomys.agent.counter;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static cn.howardliu.monitor.cynomys.agent.common.Constant.IS_DEBUG;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SLACounter {
    private static final SLACounter _COUNTER = new SLACounter();
    private volatile long sumInboundRequestCounts;
    private volatile long sumOutboundRequestCounts;
    private volatile long sumDealRequestCounts;
    private volatile long sumErrDealRequestCounts;
    private volatile long sumErrDealRequestTime;
    private volatile long sumDealRequestTime;
    private volatile long peerDealRequestTime;
    private volatile boolean isDebug;
    private Date peerDate;
    private static final Map<String, AtomicLong> responseMap = Collections
            .synchronizedMap(new HashMap<String, AtomicLong>(6, 1));

    static {
        responseMap.put("1xx", new AtomicLong());
        responseMap.put("2xx", new AtomicLong());
        responseMap.put("3xx", new AtomicLong());
        responseMap.put("4xx", new AtomicLong());
        responseMap.put("5xx", new AtomicLong());
        responseMap.put("xxx", new AtomicLong());
    }

    private SLACounter() {
    }

    public static void init() {
        _COUNTER.setDebug(IS_DEBUG);
        _COUNTER.setSumInboundRequestCounts(0);
        _COUNTER.setSumOutboundRequestCounts(0);
        _COUNTER.setSumDealRequestCounts(0);
        _COUNTER.setSumErrDealRequestCounts(0);
        _COUNTER.setSumErrDealRequestTime(0);
        _COUNTER.setSumDealRequestTime(0);
        SLACounter.setPeerDealRequestTime(0);
        _COUNTER.setPeerDate(new Date());
    }

    public static SLACounter instance() {
        return _COUNTER;
    }

    public static void addSumInboundRequestCounts() {
        _COUNTER.sumInboundRequestCounts++;
    }

    public static void addSumDealRequestCounts() {
        _COUNTER.sumDealRequestCounts++;
    }

    public static void addSumDealRequestTime(long dealTime) {
        assert dealTime >= 0;
        _COUNTER.sumDealRequestTime += dealTime;
    }

    public static void setPeerDealRequestTime(long peerDealRequestTime) {
        _COUNTER.peerDealRequestTime = peerDealRequestTime;
    }

    public static void addSumErrDealRequestTime(long dealTime) {
        assert dealTime >= 0;
        _COUNTER.sumErrDealRequestTime += dealTime;
    }

    public static void addSumErrDealRequestCounts() {
        _COUNTER.sumErrDealRequestCounts++;
    }

    public static void addSumOutboundRequestCounts() {
        _COUNTER.sumOutboundRequestCounts++;
    }

    public static void addHttpStatus(int httpStatus) {
        if (httpStatus >= 100 && httpStatus < 200) {
            responseMap.get("1xx").incrementAndGet();
        } else if (httpStatus < 300) {
            responseMap.get("2xx").incrementAndGet();
        } else if (httpStatus < 400) {
            responseMap.get("3xx").incrementAndGet();
        } else if (httpStatus < 500) {
            responseMap.get("4xx").incrementAndGet();
        } else if (httpStatus <= 599) {
            responseMap.get("5xx").incrementAndGet();
        } else {
            responseMap.get("xxx").incrementAndGet();
        }
    }

    public long getSumInboundRequestCounts() {
        return sumInboundRequestCounts;
    }

    public void setSumInboundRequestCounts(long sumInboundRequestCounts) {
        this.sumInboundRequestCounts = sumInboundRequestCounts;
    }

    public long getSumOutboundRequestCounts() {
        return sumOutboundRequestCounts;
    }

    public void setSumOutboundRequestCounts(long sumOutboundRequestCounts) {
        this.sumOutboundRequestCounts = sumOutboundRequestCounts;
    }

    public long getSumDealRequestCounts() {
        return sumDealRequestCounts;
    }

    public void setSumDealRequestCounts(long sumDealRequestCounts) {
        this.sumDealRequestCounts = sumDealRequestCounts;
    }

    public long getSumErrDealRequestCounts() {
        return sumErrDealRequestCounts;
    }

    public void setSumErrDealRequestCounts(long sumErrDealRequestCounts) {
        this.sumErrDealRequestCounts = sumErrDealRequestCounts;
    }

    public long getSumErrDealRequestTime() {
        return sumErrDealRequestTime;
    }

    public void setSumErrDealRequestTime(long sumErrDealRequestTime) {
        this.sumErrDealRequestTime = sumErrDealRequestTime;
    }

    public long getSumDealRequestTime() {
        return sumDealRequestTime;
    }

    public void setSumDealRequestTime(long sumDealRequestTime) {
        this.sumDealRequestTime = sumDealRequestTime;
    }

    public long getPeerDealRequestTime() {
        return peerDealRequestTime;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public Date getPeerDate() {
        return peerDate;
    }

    public void setPeerDate(Date peerDate) {
        this.peerDate = peerDate;
    }

    @Override
    public String toString() {
        return "SLACounter{" +
                "sumInboundRequestCounts=" + sumInboundRequestCounts +
                ", sumOutboundRequestCounts=" + sumOutboundRequestCounts +
                ", sumDealRequestCounts=" + sumDealRequestCounts +
                ", sumErrDealRequestCounts=" + sumErrDealRequestCounts +
                ", sumErrDealRequestTime=" + sumErrDealRequestTime +
                ", sumDealRequestTime=" + sumDealRequestTime +
                ", peerDealRequestTime=" + peerDealRequestTime +
                ", isDebug=" + isDebug +
                ", peerDate=" + peerDate +
                '}' +
                ",\nresponseMap{" + responseMap.toString() + '}';
    }
}

package cn.howardliu.monitor.cynomys.agent.counter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final Map<String, AtomicLong> responseMap = new HashMap<>(6, 1);

    static {
        responseMap.put("1xx", new AtomicLong());
        responseMap.put("2xx", new AtomicLong());
        responseMap.put("3xx", new AtomicLong());
        responseMap.put("4xx", new AtomicLong());
        responseMap.put("5xx", new AtomicLong());
        responseMap.put("xxx", new AtomicLong());

        init();
    }

    private AtomicLong sumInboundRequestCounts = new AtomicLong();
    private AtomicLong sumOutboundRequestCounts = new AtomicLong();
    private AtomicLong sumDealRequestCounts = new AtomicLong();
    private AtomicLong sumErrDealRequestCounts = new AtomicLong();
    private AtomicLong sumErrDealRequestTime = new AtomicLong();
    private AtomicLong sumDealRequestTime = new AtomicLong();
    private AtomicLong peerDealRequestTime = new AtomicLong();
    private AtomicBoolean isDebug = new AtomicBoolean(IS_DEBUG);
    private Date peerDate;

    private SLACounter() {
    }

    public static void init() {
        _COUNTER.setSumInboundRequestCounts(0);
        _COUNTER.setSumOutboundRequestCounts(0);
        _COUNTER.setSumDealRequestCounts(0);
        _COUNTER.setSumErrDealRequestCounts(0);
        _COUNTER.setSumErrDealRequestTime(0);
        _COUNTER.setSumDealRequestTime(0);
        _COUNTER.setPeerDate(new Date());
        setPeerDealRequestTime(0);

        resetResponseMap();
    }

    private static void resetResponseMap() {
        responseMap.get("1xx").set(0);
        responseMap.get("2xx").set(0);
        responseMap.get("3xx").set(0);
        responseMap.get("4xx").set(0);
        responseMap.get("5xx").set(0);
        responseMap.get("xxx").set(0);
    }

    public static SLACounter instance() {
        return _COUNTER;
    }

    public static void addSumInboundRequestCounts() {
        _COUNTER.sumInboundRequestCounts.incrementAndGet();
    }

    public static void addSumDealRequestCounts() {
        _COUNTER.sumDealRequestCounts.incrementAndGet();
    }

    public static void addSumDealRequestTime(long dealTime) {
        _COUNTER.sumDealRequestTime.addAndGet(dealTime);
    }

    public static void addSumErrDealRequestTime(long dealTime) {
        _COUNTER.sumErrDealRequestTime.addAndGet(dealTime);
    }

    public static void addSumErrDealRequestCounts() {
        _COUNTER.sumErrDealRequestCounts.incrementAndGet();
    }

    public static void addSumOutboundRequestCounts() {
        _COUNTER.sumOutboundRequestCounts.incrementAndGet();
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
        return sumInboundRequestCounts.get();
    }

    public void setSumInboundRequestCounts(long sumInboundRequestCounts) {
        this.sumInboundRequestCounts.set(sumInboundRequestCounts);
    }

    public long getSumOutboundRequestCounts() {
        return sumOutboundRequestCounts.get();
    }

    public void setSumOutboundRequestCounts(long sumOutboundRequestCounts) {
        this.sumOutboundRequestCounts.set(sumOutboundRequestCounts);
    }

    public long getSumDealRequestCounts() {
        return sumDealRequestCounts.get();
    }

    public void setSumDealRequestCounts(long sumDealRequestCounts) {
        this.sumDealRequestCounts.set(sumDealRequestCounts);
    }

    public long getSumErrDealRequestCounts() {
        return sumErrDealRequestCounts.get();
    }

    public void setSumErrDealRequestCounts(long sumErrDealRequestCounts) {
        this.sumErrDealRequestCounts.set(sumErrDealRequestCounts);
    }

    public long getSumErrDealRequestTime() {
        return sumErrDealRequestTime.get();
    }

    public void setSumErrDealRequestTime(long sumErrDealRequestTime) {
        this.sumErrDealRequestTime.set(sumErrDealRequestTime);
    }

    public long getSumDealRequestTime() {
        return sumDealRequestTime.get();
    }

    public void setSumDealRequestTime(long sumDealRequestTime) {
        this.sumDealRequestTime.set(sumDealRequestTime);
    }

    public long getPeerDealRequestTime() {
        return peerDealRequestTime.get();
    }

    public static void setPeerDealRequestTime(long peerDealRequestTime) {
        _COUNTER.peerDealRequestTime.set(peerDealRequestTime);
    }

    public boolean isDebug() {
        return isDebug.get();
    }

    public void setDebug(boolean isDebug) {
        this.isDebug.set(isDebug);
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

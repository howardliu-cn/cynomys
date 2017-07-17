package cn.howardliu.monitor.cynomys.agent.counter;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Counter {
    private static final Map<String, Counter> COUNTER_COLLECTOR = Collections
            .synchronizedMap(new HashMap<String, Counter>());

    protected final String counterName = "default";
    protected final String id = UUID.randomUUID().toString();
    protected final String name;
    protected volatile long hits = 0L;
    protected volatile long responseHits = 0L;
    protected volatile long maximum = 0L;
    protected volatile long durationSum = 0L;
    protected volatile long responseDurationSum = 0L;
    protected volatile long mean = 0L;// rounding num
    protected volatile long responseMean = 0L;// rounding num

    protected Counter(String name) {
        this.name = name;
    }

    public static Counter instance(String name) {
        Counter counter = COUNTER_COLLECTOR.get(name);
        if (counter == null) {
            counter = new Counter(name);
            COUNTER_COLLECTOR.put(name, counter);
        }
        ManagementFactory.getThreadMXBean().getThreadCpuTime(Thread.currentThread().getId());
        return counter;
    }

    public Counter addHit(long duration) {
        return this.addHit(duration, false);
    }

    public Counter addErrorHit(long duration) {
        return this.addHit(duration, true);
    }

    public Counter addHit(long duration, boolean isError) {
        assert duration >= 0;
        synchronized (this) {
            this.hits++;
            this.durationSum += duration;
            this.mean = this.durationSum / this.hits;
            if (!isError) {
                this.responseHits++;
                this.responseDurationSum += duration;
                this.responseMean = this.responseDurationSum / this.responseHits;
            }
        }
        this.setMaximum(duration);
        return this;
    }

    protected long setMaximum(long duration) {
        if (duration <= 0) {
            return this.maximum;
        }
        this.maximum = Math.max(duration, this.maximum);
        return this.maximum;
    }

    public String getCounterName() {
        return counterName;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getHits() {
        return hits;
    }

    public long getResponseHits() {
        return responseHits;
    }

    public long getMaximum() {
        return maximum;
    }

    public long getDurationSum() {
        return durationSum;
    }

    public long getResponseDurationSum() {
        return responseDurationSum;
    }

    public long getMean() {
        return mean;
    }

    public long getResponseMean() {
        return responseMean;
    }
}

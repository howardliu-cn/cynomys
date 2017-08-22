package cn.howardliu.monitor.cynomys.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * <br>created at 17-8-22
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum LaunchLatch {
    LATCH;

    private final CountDownLatch latch;

    LaunchLatch() {
        this.latch = new CountDownLatch(1);
    }

    public void start() {
        this.latch.countDown();
    }

    public void waitForMillis(long millis) throws InterruptedException {
        this.latch.await(millis, TimeUnit.MILLISECONDS);
    }
}

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
    STARTED, CLIENT_INIT;

    private static volatile boolean started = false;
    private final CountDownLatch latch;

    LaunchLatch() {
        this.latch = new CountDownLatch(1);
    }

    public synchronized void start() {
        if (!isStarted()) {
            this.latch.countDown();
            started = true;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean waitForMillis(long millis) throws InterruptedException {
        return this.latch.await(millis, TimeUnit.MILLISECONDS);
    }
}

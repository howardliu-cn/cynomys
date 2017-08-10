package cn.howardliu.monitor.cynomys.common;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class SemaphoreReleaseOnlyOnce {
    private final AtomicBoolean released = new AtomicBoolean(false);
    private final Semaphore semaphore;

    public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {
        this.semaphore = semaphore;
    }

    public void release() {
        if (this.semaphore != null && this.released.compareAndSet(false, true)) {
            this.semaphore.release();
        }
    }

    public Semaphore getSemaphore() {
        return semaphore;
    }
}

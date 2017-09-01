package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.common.SemaphoreReleaseOnlyOnce;
import cn.howardliu.monitor.cynomys.net.InvokeCallBack;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ResponseFuture {
    private static final Logger logger = LoggerFactory.getLogger(ResponseFuture.class);

    private final int opaque;
    private final long timeoutMillis;
    private final InvokeCallBack invokeCallBack;
    private final long beginTimestamp = System.currentTimeMillis();
    private final CountDownLatch countDownLatch = new CountDownLatch(1);

    private final SemaphoreReleaseOnlyOnce once;

    private final AtomicBoolean executeCallbackOnlyOnce = new AtomicBoolean(false);

    private volatile Message response;
    private volatile boolean sendRequestOK;
    private volatile Throwable cause;

    public ResponseFuture(int opaque, long timeoutMillis,
            InvokeCallBack invokeCallBack, SemaphoreReleaseOnlyOnce once) {
        this.opaque = opaque;
        this.timeoutMillis = timeoutMillis;
        this.invokeCallBack = invokeCallBack;
        this.once = once;
    }

    public void executeInvokeCallback() {
        if (invokeCallBack != null && this.executeCallbackOnlyOnce.compareAndSet(false, true)) {
            invokeCallBack.operationComplete(this);
        }
    }

    public void release() {
        if (this.once != null) {
            this.once.release();
        }
    }

    public boolean isTimeout() {
        return System.currentTimeMillis() - this.beginTimestamp - this.timeoutMillis > 0;
    }

    public Message waitResponse() throws InterruptedException {
        this.waitResponse(this.timeoutMillis);
        return this.response;
    }

    public void waitResponse(final long timeoutMillis) throws InterruptedException {
        if (!this.countDownLatch.await(timeoutMillis, TimeUnit.MILLISECONDS)) {
            if (logger.isDebugEnabled()) {
                logger.debug("timeout({}ms) when waiting for response", timeoutMillis);
            }
        }
    }

    public void putResponse(final Message message) {
        this.response = message;
        this.countDownLatch.countDown();
    }

    public int getOpaque() {
        return opaque;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public InvokeCallBack getInvokeCallBack() {
        return invokeCallBack;
    }

    public long getBeginTimestamp() {
        return beginTimestamp;
    }

    public boolean isSendRequestOK() {
        return sendRequestOK;
    }

    public void setSendRequestOK(boolean sendRequestOK) {
        this.sendRequestOK = sendRequestOK;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    public Message getResponse() {
        return response;
    }

    public void setResponse(Message response) {
        this.response = response;
    }

    @Override
    public String toString() {
        return "ResponseFuture{" +
                "opaque=" + opaque +
                ", timeoutMillis=" + timeoutMillis +
                ", beginTimestamp=" + beginTimestamp +
                ", countDownLatch=" + countDownLatch +
                ", response=" + response +
                ", sendRequestOK=" + sendRequestOK +
                ", cause=" + cause +
                '}';
    }
}

package cn.howardliu.monitor.cynomys.net.exception;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NetTimeoutException extends NetException {
    public NetTimeoutException(String message) {
        super(message);
    }

    public NetTimeoutException(String address, long timeoutMillis, Throwable cause) {
        super("wait response on the channel <" + address + "> timeout, " + timeoutMillis + "(ms)", cause);
    }
}

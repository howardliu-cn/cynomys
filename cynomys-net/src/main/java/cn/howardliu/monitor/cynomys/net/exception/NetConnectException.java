package cn.howardliu.monitor.cynomys.net.exception;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NetConnectException extends NetException {
    public NetConnectException(String address) {
        this(address, null);
    }

    public NetConnectException(String address, Throwable cause) {
        super("connect to <" + address + "> failed!", cause);
    }
}

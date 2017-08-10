package cn.howardliu.monitor.cynomys.net.exception;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NetSendRequestException extends NetException {
    public NetSendRequestException(String address) {
        this(address, null);
    }

    public NetSendRequestException(String address, Throwable cause) {
        super("send request to <" + address + "> failed!", cause);
    }
}

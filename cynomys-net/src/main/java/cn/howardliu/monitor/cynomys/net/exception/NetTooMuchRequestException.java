package cn.howardliu.monitor.cynomys.net.exception;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NetTooMuchRequestException extends NetException {
    public NetTooMuchRequestException(String message) {
        super(message);
    }
}

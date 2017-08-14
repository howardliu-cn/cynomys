package cn.howardliu.monitor.cynomys.net.struct;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
// TODO split code to request code and response code
public enum MessageCode {
    CONFIG_REQ((byte) 0),
    CONFIG_RESP((byte) 1),
    HEARTBEAT_REQ((byte) 2),
    HEARTBEAT_RESP((byte) 3),
    APP_INFO_REQ((byte) 4),
    APP_INFO_RESP((byte) 5),
    SQL_INFO_REQ((byte) 6),
    SQL_INFO_RESP((byte) 7),
    REQUEST_INFO_REQ((byte) 8),
    REQUEST_INFO_RESP((byte) 9);

    private byte value;

    private MessageCode(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}

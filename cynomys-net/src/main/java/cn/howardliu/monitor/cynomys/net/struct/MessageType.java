package cn.howardliu.monitor.cynomys.net.struct;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum MessageType {
    REQUEST((byte) 0),
    RESPONSE((byte) 1);

    private byte value;

    private MessageType(byte value) {
        this.value = value;
    }

    public byte value() {
        return this.value;
    }
}

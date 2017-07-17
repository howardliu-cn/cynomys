package cn.howardliu.monitor.cynomys.common;

import java.util.UUID;

/**
 * <br>created at 17-4-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Constant {
    public static final int CRC_CODE = 0x0000_0_0_1;
    public static final String THIS_TAG;

    static {
        THIS_TAG = UUID.randomUUID().toString();
    }
}

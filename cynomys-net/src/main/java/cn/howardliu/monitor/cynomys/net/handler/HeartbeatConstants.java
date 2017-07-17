package cn.howardliu.monitor.cynomys.net.handler;

import java.util.concurrent.atomic.AtomicLong;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public final class HeartbeatConstants {
    public static final AtomicLong HEARTBEAT_COUNTER = new AtomicLong(0);

    public static final int HEADER_LENGTH = 4;
}

package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.Channel;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NettyEvent {
    private final NettyEventType type;
    private final String remoteAddress;
    private final Channel channel;
    private final Throwable cause;
    private final Header header;

    public NettyEvent(NettyEventType type, String remoteAddress, Channel channel) {
        this(type, remoteAddress, channel, null, null);
    }

    public NettyEvent(NettyEventType type, String remoteAddress, Channel channel, Header header) {
        this(type, remoteAddress, channel, null, header);
    }

    public NettyEvent(NettyEventType type, String remoteAddress, Channel channel, Throwable cause) {
        this(type, remoteAddress, channel, cause, null);
    }

    public NettyEvent(NettyEventType type, String remoteAddress, Channel channel, Throwable cause, Header header) {
        this.type = type;
        this.remoteAddress = remoteAddress;
        this.channel = channel;
        this.cause = cause;
        this.header = header;
    }

    public NettyEventType getType() {
        return type;
    }

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public Channel getChannel() {
        return channel;
    }

    public Throwable getCause() {
        return cause;
    }

    public Header getHeader() {
        return header;
    }

    @Override
    public String toString() {
        return "NettyEvent{" +
                "type=" + type +
                ", remoteAddress='" + remoteAddress + '\'' +
                ", channel=" + channel +
                ", header=" + header +
                ", cause=" + cause +
                '}';
    }
}

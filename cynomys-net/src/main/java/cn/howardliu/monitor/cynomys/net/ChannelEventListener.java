package cn.howardliu.monitor.cynomys.net;

import io.netty.channel.Channel;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public interface ChannelEventListener {
    void onChannelConnect(final String address, final Channel channel);

    void onChannelClose(final String address, final Channel channel);

    void onChannelException(final String address, final Channel channel, Throwable cause);

    void onChannelIdle(final String address, final Channel channel);
}

package cn.howardliu.monitor.cynomys.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * <br>created at 17-8-8
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class ChannelHelper {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHelper.class);

    public static SocketAddress string2SocketAddress(final String address) {
        String[] s = address.split(":");
        return new InetSocketAddress(s[0], Integer.parseInt(s[1]));
    }

    public static String remoteAddress(final Channel channel) {
        if (channel == null) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String address = remote == null ? "" : remote.toString();
        if (address.isEmpty()) {
            return "";
        } else if (address.contains("/")) {
            return address.substring(address.lastIndexOf("/") + 1);
        } else {
            return address;
        }
    }

    public static void closeChannel(final Channel channel) {
        if (channel == null) {
            return;
        }
        final String remote = remoteAddress(channel);
        channel.close().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    logger.info("closeChannel: close the connection to remote address[{}] result: {}", remote,
                            future.isSuccess());
                } else {
                    logger.warn("closeChannel: close the connection to remote address[{}] result: {}", remote,
                            future.isSuccess(), future.cause());
                }
            }
        });
    }
}

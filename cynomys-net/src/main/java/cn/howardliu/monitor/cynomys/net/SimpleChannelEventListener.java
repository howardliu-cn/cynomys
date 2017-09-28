package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SimpleChannelEventListener implements ChannelEventListener {
    private static final Logger logger = LoggerFactory.getLogger(SimpleChannelEventListener.class);

    @Override
    public void onChannelConnect(String remoteAddress, Channel channel) {
        if (logger.isDebugEnabled()) {
            logger.debug("got CONNECT event, the remote address is {}, the local address is {}",
                    remoteAddress, NetHelper.localAddress(channel));
        }
    }

    @Override
    public void onChannelClose(String remoteAddress, Channel channel) {
        if (logger.isDebugEnabled()) {
            logger.debug("got CLOSE event, the remote address is {}, the local address is {}",
                    remoteAddress, NetHelper.localAddress(channel));
        }
    }

    @Override
    public void onChannelException(String address, Channel channel, Throwable cause) {
        if (logger.isDebugEnabled()) {
            logger.debug("got EXCEPTION event, the remote address is {}, the local address is {}",
                    address, NetHelper.localAddress(channel), cause);
        }
    }

    @Override
    public void onChannelIdle(String address, Channel channel) {
        if (logger.isDebugEnabled()) {
            logger.debug("got IDLE event, the remote address is {}, the local address is {}",
                    address, NetHelper.localAddress(channel));
        }
    }

    @Override
    public void onChannelRead(String address, Channel channel, Header header) {
        if (logger.isTraceEnabled()) {
            logger.trace("got READ event, the remote address is {}, the local address is {}, the header is {}-{}-{}",
                    address, NetHelper.localAddress(channel),
                    header.getSysName(), header.getSysCode(), header.getTag());
        }
    }
}

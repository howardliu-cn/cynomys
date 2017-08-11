package cn.howardliu.monitor.cynomys.net;

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
    public void onChannelConnect(String address, Channel channel) {
        logger.debug("got CONNECT event, the remote address is {}, the channel is {}", address, channel);
    }

    @Override
    public void onChannelClose(String address, Channel channel) {
        logger.debug("got CLOSE event, the remote address is {}, the channel is {}", address, channel);
    }

    @Override
    public void onChannelException(String address, Channel channel, Throwable cause) {
        logger.debug("got EXCEPTION event, the remote address is {}, the channel is {}, the cause is {}",
                address, channel, cause);
    }

    @Override
    public void onChannelIdle(String address, Channel channel) {
        logger.debug("got IDLE event, the remote address is {}, the channel is {}", address, channel);
    }
}

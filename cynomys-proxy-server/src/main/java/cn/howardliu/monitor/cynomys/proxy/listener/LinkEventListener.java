package cn.howardliu.monitor.cynomys.proxy.listener;

import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.Channel;
import org.apache.curator.framework.CuratorFramework;

/**
 * <br>created at 17-8-24
 *
 * @author liuxh
 * @since 0.0.1
 */
public class LinkEventListener extends SimpleChannelEventListener {
    public LinkEventListener(CuratorFramework zkClient) {
        LinkEventAction.ACTION.setZkClient(zkClient);
    }

    @Override
    public void onChannelRead(String address, Channel channel, Header header) {
        super.onChannelRead(address, channel, header);
        LinkEventAction.ACTION.link(channel, header);
    }

    @Override
    public void onChannelConnect(String address, Channel channel) {
        super.onChannelConnect(address, channel);
        LinkEventAction.ACTION.link(channel, null);
    }

    @Override
    public void onChannelClose(String address, Channel channel) {
        super.onChannelClose(address, channel);
        LinkEventAction.ACTION.unlink(channel);
    }

    @Override
    public void onChannelException(String address, Channel channel, Throwable cause) {
        super.onChannelException(address, channel, cause);
        LinkEventAction.ACTION.unlink(channel);
    }

    @Override
    public void onChannelIdle(String address, Channel channel) {
        super.onChannelIdle(address, channel);
        LinkEventAction.ACTION.unlink(channel);
    }
}

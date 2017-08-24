package cn.howardliu.monitor.cynomys.proxy.net;

import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.proxy.listener.LinkEventAction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-3
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class LinkCatchHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(LinkCatchHandler.class);

    public LinkCatchHandler(CuratorFramework zkClient) {
        LinkEventAction.ACTION.setZkClient(zkClient);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        LinkEventAction.ACTION.link(ctx.channel(), message.getHeader());

        ctx.fireChannelRead(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.debug("got an connection from client, the ctx is {}", ctx.toString());

        LinkEventAction.ACTION.link(ctx.channel(), null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.debug("got an disconnection from client, the ctx is {}", ctx.toString());

        LinkEventAction.ACTION.unlink(ctx.channel());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.debug("got an exception from client, the ctx is {}, the cause is {}", ctx.toString(), cause.toString());

        LinkEventAction.ACTION.unlink(ctx.channel());
    }
}

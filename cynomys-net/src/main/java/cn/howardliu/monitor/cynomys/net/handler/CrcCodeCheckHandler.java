package cn.howardliu.monitor.cynomys.net.handler;

import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-6-12
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class CrcCodeCheckHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(CrcCodeCheckHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (Constant.CRC_CODE == msg.getHeader().getCrcCode()) {
            ctx.fireChannelRead(msg);
        } else {
            logger.debug("got one message with wrong crcCode， the message is {}", msg);
        }
    }
}

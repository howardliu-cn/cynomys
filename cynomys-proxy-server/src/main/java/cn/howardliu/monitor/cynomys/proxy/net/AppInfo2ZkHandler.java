package cn.howardliu.monitor.cynomys.proxy.net;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.net.struct.MessageType.APP_INFO_REQ;

/**
 * <br>created at 17-7-29
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class AppInfo2ZkHandler extends AbstractInfo2ZkHandler {
    private static final Logger logger = LoggerFactory.getLogger(AppInfo2ZkHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message == null || message.getHeader() == null) {
            return;
        }
        Header header = message.getHeader();
        if (header.getType() == APP_INFO_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}-{}-{} send application info",
                        header.getSysCode(), header.getSysName(), header.getTag());
            }
            String prePath = "/" + header.getSysName() + "-" + header.getSysCode();
            send(ctx, message, prePath, MessageType.APP_INFO_RESP);
        } else {
            ctx.fireChannelRead(message);
        }
    }
}
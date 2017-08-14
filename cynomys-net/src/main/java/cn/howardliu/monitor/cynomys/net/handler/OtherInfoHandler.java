package cn.howardliu.monitor.cynomys.net.handler;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.net.struct.MessageCode.*;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class OtherInfoHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(OtherInfoHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new IllegalArgumentException("the message to handle cannot be null!");
        }
        Header header = msg.getHeader();
        byte code = header.getCode();
        if (code == CONFIG_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive config request: {}", msg);
            }
        } else if (code == CONFIG_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive config response: {}", msg);
            }
        } else if (code == HEARTBEAT_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive heartbeat request: {}", msg);
            }
        } else if (code == HEARTBEAT_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive heartbeat response: {}", msg);
            }
        } else if (code == APP_INFO_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive appInfo request: {}", msg);
            }
        } else if (code == APP_INFO_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive appInfo response: {}", msg);
            }
        } else if (code == SQL_INFO_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive sqlInfo request: {}", msg);
            }
        } else if (code == SQL_INFO_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive sqlInfo response: {}", msg);
            }
        } else if (code == REQUEST_INFO_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive requestInfo request: {}", msg);
            }
        } else if (code == REQUEST_INFO_RESP.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("receive requestInfo response: {}", msg);
            }
        }
        if (ctx == null) {
            throw new IllegalArgumentException(
                    "the ChannelHandlerContext which this Handler belongs to cannot be null!");
        }
        ctx.fireChannelRead(msg);
    }
}

package cn.howardliu.monitor.cynomys.net.handler;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-3-30
 *
 * @author liuxh
 * @since 0.0.1
 */
public abstract class HeartbeatHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatHandler.class);
    protected String name;

    public HeartbeatHandler(String name) {
        this.name = name;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        assert message != null;
        assert message.getHeader() != null;
        Header header = message.getHeader();
        if (header.getCode() == MessageCode.HEARTBEAT_REQ.value()) {
            // handle PING single message
            pong(ctx);
        } else if (header.getCode() == MessageCode.HEARTBEAT_RESP.value()) {
            // handle PONG single message
            if (logger.isTraceEnabled()) {
                logger.trace(name + " get PONG single message from " + ctx.channel().remoteAddress());
            }
        } else {
            ctx.fireChannelRead(message);
        }
    }

    protected void ping(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(
                new Message()
                        .setHeader(
                                customHeader()
                                        .setType(MessageType.REQUEST.value())
                                        .setCode(MessageCode.HEARTBEAT_REQ.value())
                        )
        );
        HeartbeatConstants.HEARTBEAT_COUNTER.incrementAndGet();
        if (logger.isTraceEnabled()) {
            logger.trace(name + " send PING single message to " + ctx.channel().remoteAddress());
        }
    }

    protected void pong(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(
                new Message()
                        .setHeader(
                                customHeader()
                                        .setType(MessageType.RESPONSE.value())
                                        .setCode(MessageCode.HEARTBEAT_RESP.value()))
        );
        HeartbeatConstants.HEARTBEAT_COUNTER.incrementAndGet();
        if (logger.isDebugEnabled()) {
            logger.debug(name + " send PONG single message to " + ctx.channel().remoteAddress());
        }
    }

    protected abstract Header customHeader();

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) throws Exception {
        super.userEventTriggered(ctx, event);
        if (event instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) event;
            switch (e.state()) {
                case READER_IDLE:
                    handleReaderIdle(ctx);
                    break;
                case WRITER_IDLE:
                    handleWriterIdle(ctx);
                    break;
                case ALL_IDLE:
                    handleAllIdle(ctx);
                    break;
                default:
                    logger.debug("default action with state: ", e.state());
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("got an exception", cause);
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        logger.warn("READER IDLE");
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        logger.warn("WRITER IDLE");
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        logger.warn("ALL IDLE");
    }
}

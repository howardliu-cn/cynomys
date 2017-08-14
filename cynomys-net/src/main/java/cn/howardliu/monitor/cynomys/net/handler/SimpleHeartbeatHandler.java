package cn.howardliu.monitor.cynomys.net.handler;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SimpleHeartbeatHandler extends HeartbeatHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public SimpleHeartbeatHandler(String name) {
        super(name);
    }

    @Override
    protected Header customHeader() {
        return new Header();
    }

    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        logger.debug("READER IDLE");
        handlerIdle(ctx);
    }

    protected void handleWriterIdle(ChannelHandlerContext ctx) {
        logger.debug("WRITER IDLE");
        handlerIdle(ctx);
    }

    protected void handleAllIdle(ChannelHandlerContext ctx) {
        logger.debug("ALL IDLE");
        handlerIdle(ctx);
    }

    protected void handlerIdle(ChannelHandlerContext ctx) {
        ping(ctx);
    }
}

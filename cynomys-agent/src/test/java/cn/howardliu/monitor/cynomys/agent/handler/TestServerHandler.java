package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.monitor.cynomys.net.handler.HeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.net.handler.HeartbeatConstants.HEADER_LENGTH;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 1.0.0
 */
public class TestServerHandler extends HeartbeatHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public TestServerHandler() {
        super("server");
    }

    protected void handleData(ChannelHandlerContext ctx, ByteBuf buf) {
        byte[] data = new byte[buf.readableBytes() - HEADER_LENGTH - 1];
        ByteBuf responseBuf = Unpooled.copiedBuffer(buf);
        buf.skipBytes(HEADER_LENGTH - 1);
        buf.readBytes(data);
        String content = new String(data);
        System.out.println(name + " get content: " + content);
        ctx.write(responseBuf);
    }

    @Override
    protected Header customHeader() {
        return new Header();
    }

    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("---client " + ctx.channel().remoteAddress().toString() + " reader timeout, ping it---");
        ping(ctx);
    }
}

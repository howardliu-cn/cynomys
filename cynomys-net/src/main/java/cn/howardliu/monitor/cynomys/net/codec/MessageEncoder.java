package cn.howardliu.monitor.cynomys.net.codec;

import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MessageEncoder extends MessageToByteEncoder<Message> {
    private static final Logger logger = LoggerFactory.getLogger(MessageEncoder.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        if (msg == null || msg.getHeader() == null) {
            throw new IllegalArgumentException("the encode message is null.");
        }
        out.writeInt(msg.getHeader().getOpaque());
        out.writeInt(msg.getHeader().getCrcCode());
        out.writeInt(msg.getHeader().getLength());

        out.writeInt(msg.getHeader().getTag().length());
        out.writeCharSequence(msg.getHeader().getTag(), CharsetUtil.UTF_8);

        out.writeInt(msg.getHeader().getSysName().length());
        out.writeCharSequence(msg.getHeader().getSysName(), CharsetUtil.UTF_8);

        out.writeInt(msg.getHeader().getSysCode().length());
        out.writeCharSequence(msg.getHeader().getSysCode(), CharsetUtil.UTF_8);

        out.writeByte(msg.getHeader().getType());
        out.writeByte(msg.getHeader().getCode());

        out.writeInt(msg.getHeader().getRemark().length());
        out.writeCharSequence(msg.getHeader().getRemark(), CharsetUtil.UTF_8);

        if (msg.getBody() == null) {
            out.writeInt(0);
        } else {
            out.writeInt(msg.getBody().length());
            out.writeCharSequence(msg.getBody(), CharsetUtil.UTF_8);
        }
        out.setInt(4, out.readableBytes() - 8);
    }
}

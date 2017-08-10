package cn.howardliu.monitor.cynomys.net.codec;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
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
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
    private static final Logger logger = LoggerFactory.getLogger(MessageDecoder.class);

    public MessageDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);
        if (frame == null) {
            return null;
        }
        Message message = new Message()
                .setHeader(
                        new Header()
                                .setOpaque(frame.readInt())
                                .setCrcCode(frame.readInt())
                                .setLength(frame.readInt())
                                .setTag(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setSysName(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setSysCode(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setType(frame.readByte())
                );
        if (frame.readableBytes() > 4) {
            message.setBody(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString());
        }
        return message;
    }
}

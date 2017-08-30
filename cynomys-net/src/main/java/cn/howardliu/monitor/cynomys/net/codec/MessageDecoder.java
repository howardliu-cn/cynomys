package cn.howardliu.monitor.cynomys.net.codec;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.CharsetUtil;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class MessageDecoder extends LengthFieldBasedFrameDecoder {
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
                                .setCrcCode(frame.readInt())
                                .setLength(frame.readInt())
                                .setOpaque(frame.readInt())
                                .setTag(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setSysName(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setSysCode(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString())
                                .setType(frame.readByte())
                                .setCode(frame.readByte())
                                .setFlagPath(frame.readByte())
                );
        if (frame.readableBytes() > 4) {
            message.setBody(frame.readCharSequence(frame.readInt(), CharsetUtil.UTF_8).toString());
        }
        return message;
    }
}

package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public interface NettyRequestProcessor {
    Message processRequest(ChannelHandlerContext ctx, Message request) throws Exception;

    default boolean waitResponse() {
        return true;
    }

    default boolean rejectRequest() {
        return false;
    }
}

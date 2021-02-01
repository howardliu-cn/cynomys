package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.monitor.cynomys.net.netty.NettyRequestProcessor;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.net.struct.MessageCode.HEARTBEAT_RESP;
import static cn.howardliu.monitor.cynomys.net.struct.MessageType.RESPONSE;

/**
 * <br>created at 17-8-24
 *
 * @author liuxh
 * @since 0.0.1
 */
public class HeartbeatProcessor implements NettyRequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(HeartbeatProcessor.class);

    @Override
    public Message processRequest(ChannelHandlerContext ctx, Message request) throws Exception {
        if (logger.isTraceEnabled()) {
            Header header = request.getHeader();
            logger.trace("{}-{}-{} send PING signal", header.getSysName(), header.getSysCode(), header.getTag());
        }
        return new Message()
                .setHeader(new Header()
                        .setType(RESPONSE.value())
                        .setCode(HEARTBEAT_RESP.value())
                );
    }
}

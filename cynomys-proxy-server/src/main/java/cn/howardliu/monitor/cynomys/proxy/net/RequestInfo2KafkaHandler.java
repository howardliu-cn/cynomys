package cn.howardliu.monitor.cynomys.proxy.net;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.net.struct.MessageCode.REQUEST_INFO_REQ;
import static cn.howardliu.monitor.cynomys.proxy.config.SystemSetting.SYSTEM_SETTING;

/**
 * <br>created at 17-7-29
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class RequestInfo2KafkaHandler extends AbstractInfo2KafkaHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestInfo2KafkaHandler.class);

    public RequestInfo2KafkaHandler(KafkaProducerWrapper<String, String> kafkaProducerWrapper) {
        super(kafkaProducerWrapper);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        if (message == null || message.getHeader() == null) {
            return;
        }
        Header header = message.getHeader();
        if (header.getCode() == REQUEST_INFO_REQ.value()) {
            if (logger.isDebugEnabled()) {
                logger.debug("{}-{}-{} send request info",
                        header.getSysCode(), header.getSysName(), header.getTag());
            }
            send(ctx, message, SYSTEM_SETTING.getKafkaTopicRequest(), MessageCode.REQUEST_INFO_RESP);
        } else {
            ctx.fireChannelRead(message);
        }
    }
}

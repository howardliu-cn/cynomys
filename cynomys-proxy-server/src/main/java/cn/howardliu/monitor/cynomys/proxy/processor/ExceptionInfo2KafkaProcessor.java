package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.proxy.config.SystemSetting.SYSTEM_SETTING;

/**
 * <br>created at 17-8-24
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ExceptionInfo2KafkaProcessor extends AbstractInfo2KafkaProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionInfo2KafkaProcessor.class);

    public ExceptionInfo2KafkaProcessor(KafkaProducerWrapper<String, String> kafkaProducerWrapper) {
        super(kafkaProducerWrapper);
    }

    @Override
    public Message processRequest(ChannelHandlerContext ctx, Message request) throws Exception {
        if (logger.isDebugEnabled()) {
            final Header header = request.getHeader();
            logger.debug("{}-{}-{} send exception info", header.getSysName(), header.getSysCode(), header.getTag());
        }
        send(ctx, request, SYSTEM_SETTING.getKafkaTopicException(), MessageCode.EXCEPTION_INFO_RESP);
        return null;
    }
}

package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;

import static cn.howardliu.monitor.cynomys.proxy.config.SystemSetting.SYSTEM_SETTING;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SqlInfo2KafkaProcessor extends AbstractInfo2KafkaProcessor {
    public SqlInfo2KafkaProcessor(KafkaProducerWrapper<String, String> kafkaProducerWrapper) {
        super(kafkaProducerWrapper);
    }

    @Override
    public Message processRequest(ChannelHandlerContext ctx, Message request) throws Exception {
        send(ctx, request, SYSTEM_SETTING.getKafkaTopicSql());
        return null;
    }
}

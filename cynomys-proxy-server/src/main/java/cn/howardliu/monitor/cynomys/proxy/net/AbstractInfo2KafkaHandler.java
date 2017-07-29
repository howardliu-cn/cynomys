package cn.howardliu.monitor.cynomys.proxy.net;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.Properties;

import static cn.howardliu.monitor.cynomys.proxy.config.SystemSetting.SYSTEM_SETTING;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * <br>created at 17-7-29
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class AbstractInfo2KafkaHandler extends SimpleChannelInboundHandler<Message> {
    private final KafkaProducerWrapper<String, String> kafkaProducerWrapper;

    public AbstractInfo2KafkaHandler() {
        Properties config = new Properties();
        config.put(BOOTSTRAP_SERVERS_CONFIG, SYSTEM_SETTING.getKafkaBootstrapServers());
        config.put(ACKS_CONFIG, SYSTEM_SETTING.getKafkaAcks());
        config.put(RETRIES_CONFIG, SYSTEM_SETTING.getKafkaRetries());
        config.put(BATCH_SIZE_CONFIG, SYSTEM_SETTING.getKafkaBatchSize());
        config.put(MAX_REQUEST_SIZE_CONFIG, SYSTEM_SETTING.getKafkaMaxRequestSize());
        kafkaProducerWrapper = new KafkaProducerWrapper<>(config);
    }

    protected void send(ChannelHandlerContext ctx, Message message, String topic, MessageType resp) {
        Header header = message.getHeader();
        kafkaProducerWrapper.send(
                topic,
                header.getSysCode() + "-" + header.getSysName() + "-" + header.getTag(),
                message.getBody(),
                (key, value, metadata, exception) -> {
                    boolean success = true;
                    String errMsg = null;
                    if (exception != null) {
                        success = false;
                        errMsg = exception.toString();
                    }
                    StringBuilder body = new StringBuilder();
                    if (success) {
                        body.append("{success: true}");
                    } else {
                        body.append("{success: false, errMsg: \"").append(errMsg).append("\"}");
                    }

                    ctx.writeAndFlush(
                            new Message()
                                    .setHeader(new Header().setType(resp.value()))
                                    .setBody(body.toString())
                    );
                }
        );
    }
}

package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.monitor.cynomys.net.netty.NettyRequestProcessor;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.ChannelHandlerContext;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public abstract class AbstractInfo2KafkaProcessor implements NettyRequestProcessor {
    private final KafkaProducerWrapper<String, String> kafkaProducerWrapper;

    public AbstractInfo2KafkaProcessor(KafkaProducerWrapper<String, String> kafkaProducerWrapper) {
        this.kafkaProducerWrapper = kafkaProducerWrapper;
    }

    @Override
    public boolean waitResponse() {
        return false;
    }

    protected void send(ChannelHandlerContext ctx, Message request, String topic) {
        Header header = request.getHeader();
        int opaque = header.getOpaque();
        kafkaProducerWrapper.send(
                topic,
                header.getSysCode() + "-" + header.getSysName() + "-" + header.getTag(),
                request.getBody(),
                (key, value, metadata, exception) -> {
                    boolean success = true;
                    String errMsg = null;
                    if (exception != null) {
                        success = false;
                        errMsg = exception.toString();
                    }
                    StringBuilder body = new StringBuilder();
                    if (success) {
                        body.append("");
                    } else {
                        body.append(errMsg);
                    }

                    ctx.writeAndFlush(
                            new Message()
                                    .setHeader(
                                            new Header()
                                                    .setType(MessageType.RESPONSE.value())
                                                    .setOpaque(opaque)
                                    )
                                    .setBody(body.toString())
                    );
                }
        );
    }
}

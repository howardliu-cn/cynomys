package cn.howardliu.monitor.cynomys.agent.net.handler;

import cn.howardliu.monitor.cynomys.agent.net.ServerInfo;
import cn.howardliu.monitor.cynomys.agent.net.operator.ConfigInfoOperator;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.agent.net.ServerInfo.ServerType.LAN;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConfigInfoHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(ConfigInfoHandler.class);
    private ConfigInfoOperator operator;

    public ConfigInfoHandler(ConfigInfoOperator operator) {
        this.operator = operator;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        query(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (msg == null || msg.getHeader() == null || msg.getBody() == null) {
            ctx.fireChannelRead(msg);
            return;
        }
        if (MessageType.CONFIG_RESP.value() == msg.getHeader().getType()) {
            if (StringUtils.isBlank(msg.getBody())) {
                query(ctx);
            }
            try {
                JSONArray array = JSON.parseArray(msg.getBody());
                if (logger.isDebugEnabled()) {
                    logger.debug("receive message: {}", msg);
                }
                if (array.size() == 0) {
                    ctx.writeAndFlush(new Message().setHeader(new Header().setType(MessageType.CONFIG_REQ.value())));
                    return;
                }
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    this.operator.addServerInfo(
                            new ServerInfo(
                                    json.getString("ip"),
                                    json.getInteger("port"),
                                    json.getInteger("connectCount"),
                                    LAN
                            )
                    );
                }
                this.operator.read();
                ctx.close().addListener(ChannelFutureListener.CLOSE);
            } catch (Exception e) {
                query(ctx);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    private void query(ChannelHandlerContext ctx) {
        ctx.writeAndFlush(new Message().setHeader(new Header().setType(MessageType.CONFIG_REQ.value())));
    }
}

package cn.howardliu.monitor.cynomys.agent.net.handler;

import cn.howardliu.monitor.cynomys.agent.net.operator.SocketMonitorDataOperator;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.handler.SimpleHeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public class CustomHeartbeatHandler extends SimpleHeartbeatHandler {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private SocketMonitorDataOperator handler;

    public CustomHeartbeatHandler(String name, SocketMonitorDataOperator handler) {
        super(name);
        this.handler = handler;
    }

    @Override
    protected Header customHeader() {
        return super.customHeader()
                .setSysName(Constant.SYS_NAME)
                .setSysCode(Constant.SYS_CODE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("net error, reconnect now.", cause);
        this.handler.connect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        // reconnect
        this.handler.connect();
    }
}

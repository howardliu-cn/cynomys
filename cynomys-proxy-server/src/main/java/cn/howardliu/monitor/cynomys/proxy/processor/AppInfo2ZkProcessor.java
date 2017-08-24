package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import io.netty.channel.ChannelHandlerContext;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public class AppInfo2ZkProcessor extends AbstractInfo2ZkProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AppInfo2ZkProcessor.class);

    public AppInfo2ZkProcessor(CuratorFramework zkClient) {
        super(zkClient);
    }

    @Override
    public Message processRequest(ChannelHandlerContext ctx, Message request) throws Exception {
        Header header = request.getHeader();
        if (logger.isDebugEnabled()) {
            logger.debug("{}-{}-{} send application info",
                    header.getSysName(), header.getSysCode(), header.getTag());
        }
        Message response = send(ctx, request, "/" + header.getSysName() + "-" + header.getSysCode());
        response.getHeader().setCode(MessageCode.APP_INFO_RESP.value());
        return response;
    }
}

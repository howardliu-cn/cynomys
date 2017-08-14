package cn.howardliu.monitor.cynomys.proxy.processor;

import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.netty.NettyRequestProcessor;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import cn.howardliu.monitor.cynomys.proxy.config.SystemSetting;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.CharEncoding;
import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public abstract class AbstractInfo2ZkProcessor implements NettyRequestProcessor {
    private static final Logger logger = LoggerFactory.getLogger(AbstractInfo2ZkProcessor.class);

    private final CuratorFramework zkClient;

    public AbstractInfo2ZkProcessor(CuratorFramework zkClient) {
        this.zkClient = Validate.notNull(zkClient, "the CuratorFramework object cannot be null");
    }

    protected Message send(ChannelHandlerContext ctx, Message message, String prePath) {
        Header header = message.getHeader();
        int opaque = header.getOpaque();
        String path = prePath + "/" + header.getSysName() + "-" + header.getSysCode() + "-" + header.getTag();
        boolean success = true;
        String errMsg = null;
        try {
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null && SystemSetting.SYSTEM_SETTING.isCreatePathIfNeeded()) {
                try {
                    String data = header.getSysName() + "-" + header.getSysCode();
                    zkClient.create().creatingParentsIfNeeded()
                            .withMode(CreateMode.EPHEMERAL)
                            .forPath(path, data.getBytes(CharEncoding.UTF_8));
                    stat = zkClient.checkExists().forPath(path);
                } catch (Exception e) {
                    stat = zkClient.checkExists().forPath(path);
                    if (stat == null) {
                        throw new RuntimeException("got an exception when creating path [" + path + "]");
                    }
                }
            }
            if (stat != null) {
                zkClient.setData().forPath(path, message.getBody().getBytes(CharEncoding.UTF_8));
            }
        } catch (Exception e) {
            logger.error("got an exception when sending application info to zk", e);
            success = false;
            errMsg = NetHelper.exceptionSimpleDesc(e);
        }

        StringBuilder body = new StringBuilder();
        if (success) {
            body.append("");
        } else {
            body.append(errMsg);
        }
        return
                new Message()
                        .setHeader(
                                new Header()
                                        .setType(MessageType.RESPONSE.value())
                                        .setOpaque(opaque)
                        )
                        .setBody(body.toString());
    }
}

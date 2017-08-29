package cn.howardliu.monitor.cynomys.proxy.listener;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.Channel;
import org.apache.commons.lang3.CharEncoding;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <br>created at 17-8-24
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum LinkEventAction {
    ACTION(null);

    private static final Logger logger = LoggerFactory.getLogger(LinkEventAction.class);
    private static final Map<Channel, Header> ctxSets = new ConcurrentHashMap<>();
    private CuratorFramework zkClient;

    LinkEventAction(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void setZkClient(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    public void link(Channel channel, Header header) {
        if (ctxSets.get(channel) == null
                || (header != null && header.equals(ctxSets.get(channel)))) {
            logger.debug("link event from client, create monitor path, header={}, channel={}",
                    channel.toString(), header);
            ctxSets.put(channel, header);
        }
        createLinkFlag(header);
    }

    public void unlink(Channel channel) {
        if (logger.isDebugEnabled()) {
            Header header = ctxSets.get(channel);
            if (header != null) {
                logger.debug("unlink event from client, remove monitor path, header={}, channel={}",
                        channel.toString(), header);
            }
        }
        removeLinkFlag(ctxSets.remove(channel));
    }

    private void createLinkFlag(Header header) {
        if (header == null || header.getFlagPath() == 0) {
            return;
        }
        try {
            String path = getPath(header);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                //noinspection StringBufferReplaceableByString
                zkClient.create().creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .forPath(
                                path,
                                new StringBuilder()
                                        .append('{')
                                        .append("\"tag\":\"")
                                        .append(header.getTag())
                                        .append("\",")
                                        .append("\"sysName\":\"")
                                        .append(header.getSysName())
                                        .append("\",")
                                        .append("\"sysCode\":\"")
                                        .append(header.getSysCode())
                                        .append("\",")
                                        .append("\"status\":\"connected\"")
                                        .append('}')
                                        .toString().getBytes(CharEncoding.UTF_8)
                        );
            }
        } catch (UnsupportedEncodingException ignored) {
        } catch (Exception e) {
            logger.error("got and exception when creating path \"{}-{}-{}\" in zookeeper",
                    header.getSysName(), header.getSysCode(), header.getTag(), e);
        }
    }

    private void removeLinkFlag(Header header) {
        if (header == null) {
            return;
        }
        try {
            String path = getPath(header);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat != null) {
                zkClient.delete().deletingChildrenIfNeeded().forPath(path);
            }
        } catch (UnsupportedEncodingException ignored) {
        } catch (Exception e) {
            logger.error("got and exception when deleting path \"{}-{}-{}\" in zookeeper",
                    header.getSysName(), header.getSysCode(), header.getTag(), e);
        }
    }

    private String getPath(Header header) {
        //noinspection StringBufferReplaceableByString
        return new StringBuilder()
                .append('/')
                .append(header.getSysName())
                .append('-')
                .append(header.getSysCode())
                .append('/')
                .append(header.getSysName())
                .append('-')
                .append(header.getSysCode())
                .append('-')
                .append(header.getTag())
                .toString();
    }
}

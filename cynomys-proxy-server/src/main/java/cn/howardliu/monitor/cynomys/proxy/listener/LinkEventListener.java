package cn.howardliu.monitor.cynomys.proxy.listener;

import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.channel.Channel;
import org.apache.commons.lang3.CharEncoding;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
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
public class LinkEventListener extends SimpleChannelEventListener {
    private static final Logger logger = LoggerFactory.getLogger(LinkEventListener.class);

    public LinkEventListener(CuratorFramework zkClient) {
        LinkEventAction.ACTION.setZkClient(zkClient);
    }

    @Override
    public void onChannelRead(String address, Channel channel, Header header) {
        super.onChannelRead(address, channel, header);
        LinkEventAction.ACTION.link(channel, header);
    }

    @Override
    public void onChannelConnect(String address, Channel channel) {
        super.onChannelConnect(address, channel);
        LinkEventAction.ACTION.link(channel, null);
    }

    @Override
    public void onChannelClose(String address, Channel channel) {
        super.onChannelClose(address, channel);
        LinkEventAction.ACTION.unlink(channel);
    }

    @Override
    public void onChannelException(String address, Channel channel, Throwable cause) {
        super.onChannelException(address, channel, cause);
        LinkEventAction.ACTION.unlink(channel);
    }

    @Override
    public void onChannelIdle(String address, Channel channel) {
        super.onChannelIdle(address, channel);
        LinkEventAction.ACTION.unlink(channel);
    }

    enum LinkEventAction {
        ACTION;

        private static final Map<String, Header> ctxSets = new ConcurrentHashMap<>();
        private static CuratorFramework zkClient;

        LinkEventAction() {
        }

        private void setZkClient(CuratorFramework zkClient) {
            LinkEventAction.zkClient = zkClient;
        }

        public void link(Channel channel, Header header) {
            if (logger.isTraceEnabled()) {
                logger.trace("link event from client, channel={}", channel.toString());
            }
            if (header == null) {
                return;
            }
            String channelId = channel.id().asLongText();
            if (ctxSets.get(channelId) == null || !ctxSets.get(channelId).equals(header)) {
                ctxSets.put(channelId, header);
            }
            createLinkFlag(header);
        }

        public void unlink(Channel channel) {
            String channelId = channel.id().asLongText();
            if (logger.isDebugEnabled()) {
                Header header = ctxSets.get(channelId);
                if (header != null) {
                    logger.debug("unlink event from client, remove monitor path, header={}, channel={}",
                            channel.toString(), header);
                }
            }
            removeLinkFlag(ctxSets.remove(channelId));
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
            } catch (UnsupportedEncodingException e) {
                logger.error("if you got this exception, please check your JDK version as soon as possible", e);
            } catch (KeeperException.NodeExistsException e) {
                logger.debug(e.getMessage());
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
            } catch (UnsupportedEncodingException e) {
                logger.error("if you got this exception, please check your JDK version as soon as possible", e);
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
}

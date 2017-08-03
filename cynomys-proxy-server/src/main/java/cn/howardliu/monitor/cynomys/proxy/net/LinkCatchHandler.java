package cn.howardliu.monitor.cynomys.proxy.net;

import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.lang3.CharEncoding;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * <br>created at 17-8-3
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class LinkCatchHandler extends SimpleChannelInboundHandler<Message> {
    private static final Logger logger = LoggerFactory.getLogger(LinkCatchHandler.class);
    private static final Map<ChannelHandlerContext, Header> ctxSets = Collections.synchronizedMap(new HashMap<>());
    private final CuratorFramework zkClient;

    public LinkCatchHandler(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        Header header = null;
        if (ctxSets.get(ctx) == null
                || (message.getHeader() != null && !ctxSets.get(ctx).equals(message.getHeader()))) {
            logger.debug("fill the connection and ctx, header={}, ctx={}", ctx.toString(), message.getHeader());
            ctxSets.put(ctx, message.getHeader());
            header = message.getHeader();
        }
        createLinkFlag(header);
        ctx.fireChannelRead(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        logger.debug("got an connection from client, the ctx is {}", ctx.toString());
        ctxSets.put(ctx, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        logger.debug("got an disconnection from client, the ctx is {}", ctx.toString());
        removeLinkFlag(ctxSets.remove(ctx));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.debug("got an exception from client, the ctx is {}, the cause is {}", ctx.toString(), cause.toString());
        removeLinkFlag(ctxSets.remove(ctx));
    }

    private void createLinkFlag(Header header) {
        if (header == null) {
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

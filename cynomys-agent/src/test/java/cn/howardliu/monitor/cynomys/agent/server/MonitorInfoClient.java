package cn.howardliu.monitor.cynomys.agent.server;

import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.HeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-5-22
 *
 * @author liuxh
 * @version 1.0.0
 * @since 1.0.0
 */
public class MonitorInfoClient {
    private static final Logger logger = LoggerFactory.getLogger(MonitorInfoClient.class);

    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup workGroup = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 protected void initChannel(SocketChannel ch) throws Exception {
                                     ch.pipeline()
                                             .addLast("idle-state-handler", new IdleStateHandler(0, 0, 5))
                                             .addLast("MessageDecoder", new MessageDecoder(1024 * 1024 * 100, 4, 4))
                                             .addLast("MessageEncoder", new MessageEncoder())
                                             .addLast("read-timeout-handler", new ReadTimeoutHandler(50))
                                             .addLast("HeartbeatHandler", new HeartbeatHandler("test-client") {
                                                 protected void handleReaderIdle(ChannelHandlerContext ctx) {
                                                     logger.warn("READER IDLE");
                                                     handlerIdle(ctx);
                                                 }

                                                 protected void handleWriterIdle(ChannelHandlerContext ctx) {
                                                     logger.warn("WRITER IDLE");
                                                     handlerIdle(ctx);
                                                 }

                                                 protected void handleAllIdle(ChannelHandlerContext ctx) {
                                                     logger.warn("ALL IDLE");
                                                     handlerIdle(ctx);
                                                 }

                                                 private void handlerIdle(ChannelHandlerContext ctx) {
                                                     ping(ctx);
                                                 }
                                             })
                                             .addLast("OtherInfoHandler", new OtherInfoHandler());
                                 }
                             }
                    ).connect("192.168.7.21", 1666)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Connect to server successfully!");
                                }
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Failed to connect to server , try connect after {}s");
                                }
                            }
                        }
                    }).sync()
                    .channel().closeFuture().sync();
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}

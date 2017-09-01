package cn.howardliu.monitor.cynomys.proxy.config;

import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.handler.SimpleHeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @since 0.0.1
 */
public class NettyNetClientTest {
    private static final Logger logger = LoggerFactory.getLogger(NettyNetClient.class);

    @Test
    public void test() throws InterruptedException {
        NettyNetClient client = new NettyNetClient(new NettyClientConfig());
        client.start();
        client.connect();
    }

    class NettyNetClient {
        private final NettyClientConfig nettyClientConfig;
        private final EventLoopGroup eventLoopGroupWorker;
        private final Bootstrap bootstrap = new Bootstrap();
        private final AtomicInteger relinkCount = new AtomicInteger();

        NettyNetClient(final NettyClientConfig nettyClientConfig) {
            this.nettyClientConfig = nettyClientConfig;

            this.eventLoopGroupWorker = new NioEventLoopGroup(
                    1,
                    new ThreadFactory() {
                        private AtomicInteger threadIndex = new AtomicInteger();

                        @Override
                        public Thread newThread(Runnable r) {
                            return new Thread(r, "netty-client-selector-" + this.threadIndex.incrementAndGet());
                        }
                    }
            );
        }

        void start() {
            this.bootstrap
                    .group(this.eventLoopGroupWorker)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.SO_KEEPALIVE, false)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                    .option(ChannelOption.SO_SNDBUF, 65535)
                    .option(ChannelOption.SO_RCVBUF, 65535)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new ReadTimeoutHandler(60))
                                    .addLast(new IdleStateHandler(0, 0,
                                            nettyClientConfig.getClientChannelMaxIdleTimeSeconds()))
                                    .addLast(
                                            new MessageDecoder(nettyClientConfig.getClientSocketMaxFrameLength(), 4, 4))
                                    .addLast(new MessageEncoder())
                                    .addLast(new SimpleHeartbeatHandler(nettyClientConfig.getClientName()) {
                                        @Override
                                        protected Header customHeader() {
                                            return super.customHeader()
                                                    .setSysName(Constant.sysName)
                                                    .setSysCode(Constant.sysCode);
                                        }
                                    })
                                    .addLast(new OtherInfoHandler() {
                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            cause.printStackTrace();
                                        }
                                    })
                            ;
                        }
                    });
        }

        void connect() throws InterruptedException {
            String address = "127.0.0.1:7911";
            this.bootstrap.connect(NetHelper.string2SocketAddress(address))
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Connect to server [{}] successfully!", address);
                            }
                        } else {
                            if (relinkCount.get() <= this.nettyClientConfig.getRelinkMaxCount()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Failed to connect to server [{}], try connect after {}ms, this is {} times.",
                                            address, this.nettyClientConfig.getRelinkDelayMillis(), relinkCount.get());
                                }
                                future.channel().eventLoop().schedule(
                                        () -> {
                                            try {
                                                relinkCount.incrementAndGet();
                                                connect();
                                            } catch (InterruptedException ignored) {
                                            }
                                        },
                                        this.nettyClientConfig.getRelinkDelayMillis(),
                                        TimeUnit.MILLISECONDS
                                );
                            } else {
                                if (logger.isDebugEnabled()) {
                                    logger.debug(
                                            "Failed to connect to server [{}], and reconnect {} times, do close!",
                                            address, relinkCount.get());
                                }
                                relinkCount.set(0);
                            }
                        }
                    }).sync()
                    .channel().closeFuture().sync();
        }
    }

}

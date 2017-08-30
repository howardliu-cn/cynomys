package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.common.Pair;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.NetServer;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.howardliu.monitor.cynomys.net.struct.MessageType.REQUEST;

/**
 * <br>created at 17-8-14
 *
 * @author liuxh
 * @since 0.0.1
 */
public class NettyNetServer extends NettyNetAbstract implements NetServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyNetServer.class);
    private final ServerBootstrap serverBootstrap;
    private final EventLoopGroup eventLoopGroupSelector;
    private final EventLoopGroup eventLoopGroupBoss;
    private final NettyServerConfig nettyServerConfig;
    private final ExecutorService publicExecutor;
    private final ChannelEventListener channelEventListener;

    private final Timer timer = new Timer("", true);

    private DefaultEventExecutorGroup defaultEventExecutorGroup;
    private int port = 0;

    public NettyNetServer(final NettyServerConfig nettyServerConfig) {
        this(nettyServerConfig, null);
    }

    public NettyNetServer(final NettyServerConfig nettyServerConfig, final ChannelEventListener channelEventListener) {
        super(nettyServerConfig.getServerAsyncSemaphoreValue());

        this.nettyServerConfig = nettyServerConfig;
        this.channelEventListener = channelEventListener;
        this.serverBootstrap = new ServerBootstrap();

        int publicThreads = nettyServerConfig.getServerCallbackExecutorThreads();
        if (publicThreads <= 0) {
            publicThreads = 4;
        }

        this.publicExecutor = Executors.newFixedThreadPool(
                publicThreads,
                new DefaultThreadFactory("netty-server-public-executor")
        );

        this.eventLoopGroupBoss = new NioEventLoopGroup(
                1,
                new DefaultThreadFactory(
                        "netty-boss" + nettyServerConfig.getServerSelectorThreads())
        );

        if (NetHelper.isLinuxPlatform() && nettyServerConfig.isUseEpollNativeSelector()) {
            this.eventLoopGroupSelector = new EpollEventLoopGroup(
                    nettyServerConfig.getServerSelectorThreads(),
                    new DefaultThreadFactory(
                            "netty-server-epoll-selector-" + nettyServerConfig.getServerSelectorThreads())
            );
        } else {
            this.eventLoopGroupSelector = new NioEventLoopGroup(
                    nettyServerConfig.getServerSelectorThreads(),
                    new DefaultThreadFactory(
                            "netty-server-nio-selector-" + nettyServerConfig.getServerSelectorThreads())
            );
        }
    }

    @Override
    public void start() {
        this.start = true;
        this.stop = false;

        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nettyServerConfig.getServerWorkerThreads(),
                new DefaultThreadFactory("netty-server-default-event-executor")
        );

        ServerBootstrap childHandler = this.serverBootstrap
                .group(this.eventLoopGroupBoss, this.eventLoopGroupSelector)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_KEEPALIVE, false)
                .option(ChannelOption.SO_SNDBUF, nettyServerConfig.getServerSocketSndBufSize())
                .option(ChannelOption.SO_RCVBUF, nettyServerConfig.getServerSocketRcvBufSize())
                .childOption(ChannelOption.TCP_NODELAY, true)
                .localAddress(this.nettyServerConfig.getListenPort())
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(this.getChannelHandler());

        if (nettyServerConfig.isServerPooledByteBufAllocatorEnable()) {
            childHandler.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
        }

        try {
            ChannelFuture sync = this.serverBootstrap.bind().sync();
            this.port = ((InetSocketAddress) sync.channel().localAddress()).getPort();
        } catch (InterruptedException e) {
            throw new RuntimeException("this.serverBootstrap.bind().sync() Interrupted", e);
        }

        if (this.channelEventListener != null) {
            this.nettyEventExecutor.start();
        }

        this.timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                try {
                    scanResponseTable();
                } catch (Exception e) {
                    logger.error("scanResponseTable exception", e);
                }
            }
        }, 1000 * 3, 1000);
    }

    protected ChannelHandler getChannelHandler() {
        return new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast(new IdleStateHandler(0, 0,
                                nettyServerConfig.getServerChannelMaxIdleTimeSeconds()))
                        .addLast(new MessageDecoder(nettyServerConfig.getServerSocketMaxFrameLength(), 4, 4))
                        .addLast(new MessageEncoder())
                        .addLast(new NettyConnectManageHandler())
                        .addLast(additionalChannelHandler())
                        .addLast(additionalChannelHandler2())
                        .addLast(new OtherInfoHandler() {
                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                    throws Exception {
                                cause.printStackTrace();
                            }
                        });
            }
        };
    }

    protected ChannelHandler[] additionalChannelHandler() {
        return new ChannelHandler[0];
    }

    protected ChannelHandler[] additionalChannelHandler2() {
        return new ChannelHandler[]{
                new SimpleChannelInboundHandler<Message>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
                        byte rpcType = msg.getHeader().getType();
                        if (rpcType == REQUEST.value()) {
                            processRequest(ctx, msg);
                        } else {
                            ctx.fireChannelRead(msg);
                        }
                    }
                }
        };
    }

    @Override
    public void shutdown() {
        this.start = false;
        this.stop = true;

        try {
            if (this.timer != null) {
                this.timer.cancel();
            }

            this.eventLoopGroupBoss.shutdownGracefully();
            this.eventLoopGroupSelector.shutdownGracefully();

            if (this.nettyEventExecutor != null) {
                this.nettyEventExecutor.shutdown();
            }

            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }

            if (this.publicExecutor != null) {
                this.publicExecutor.shutdown();
            }
        } catch (Exception e) {
            logger.error("NettyNetServer shutdown exception", e);
        }

    }

    @Override
    public boolean isStopped() {
        return this.stop;
    }

    @Override
    public boolean isStarted() {
        return this.start;
    }

    @Override
    public ChannelEventListener getChannelEventListener() {
        return this.channelEventListener;
    }

    @Override
    public ExecutorService getCallbackExecutor() {
        return this.publicExecutor;
    }

    @Override
    public int localListenPort() {
        return this.port;
    }

    @Override
    public void registProcessor(byte requestCode, NettyRequestProcessor processor,
            ExecutorService executor) {
        ExecutorService _executor = executor;
        if (executor == null) {
            _executor = this.publicExecutor;
        }
        this.processorTable.put(requestCode, new Pair<>(processor, _executor));
    }

    @Override
    public void registDefaultProcessor(NettyRequestProcessor processor, ExecutorService executor) {
        this.defaultRequestProcessor = new Pair<>(processor, executor);
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY SERVER PIPELINE: channelRead {}", remoteAddress);
            if (channelEventListener != null && msg instanceof Message) {
                Message m = (Message) msg;
                putNettyEvent(new NettyEvent(NettyEventType.READ, remoteAddress, ctx.channel(), m.getHeader()));
            }
            super.channelRead(ctx, msg);
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY SERVER PIPELINE: channelActive {}", remoteAddress);
            super.channelActive(ctx);

            if (channelEventListener != null) {
                putNettyEvent(new NettyEvent(NettyEventType.CONNECT, remoteAddress, ctx.channel()));
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY SERVER PIPELINE: channelInactive {}", remoteAddress);
            super.channelInactive(ctx);

            if (channelEventListener != null) {
                putNettyEvent(new NettyEvent(NettyEventType.CLOSE, remoteAddress, ctx.channel()));
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY SERVER PIPELINE: exceptionCaught {}", remoteAddress);
            super.exceptionCaught(ctx, cause);

            if (channelEventListener != null) {
                putNettyEvent(new NettyEvent(NettyEventType.EXCEPTION, remoteAddress, ctx.channel(), cause));
            }

            NetHelper.closeChannel(ctx.channel());
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state().equals(IdleState.ALL_IDLE)) {
                    final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
                    logger.warn("NETTY SERVER PIPELINE: IDLE exception [{}]", remoteAddress);
                    NetHelper.closeChannel(ctx.channel());
                    if (channelEventListener != null) {
                        putNettyEvent(new NettyEvent(NettyEventType.IDLE, remoteAddress, ctx.channel()));
                    }
                }
            }

            ctx.fireUserEventTriggered(evt);
        }
    }
}

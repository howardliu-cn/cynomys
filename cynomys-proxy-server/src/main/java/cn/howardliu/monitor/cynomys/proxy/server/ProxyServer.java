package cn.howardliu.monitor.cynomys.proxy.server;

import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.HeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.proxy.ServerContext;
import cn.howardliu.monitor.cynomys.proxy.net.AppInfo2KafkaHandler;
import cn.howardliu.monitor.cynomys.proxy.net.AppInfo2ZkHandler;
import cn.howardliu.monitor.cynomys.proxy.net.RequestInfo2KafkaHandler;
import cn.howardliu.monitor.cynomys.proxy.net.SqlInfo2KafkaHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static cn.howardliu.monitor.cynomys.proxy.config.ProxyConfig.PROXY_CONFIG;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProxyServer extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerContext context;

    public ProxyServer() {
        this(PROXY_CONFIG.getPort(), PROXY_CONFIG.getCport());
    }

    public ProxyServer(int port, int cport) {
        super(port, cport);
    }

    public void startup() {
        logger.info("begin to starting, use server port [], control port []", port, cport);
        this.context = ServerContext.getInstance(this);
        try {
            ctrl();
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    // read maxFrameLength from config file
                                    .addLast("MessageDecoder",
                                            new MessageDecoder(PROXY_CONFIG.getMaxFrameLength(), 4, 4))
                                    .addLast("MessageEncoder", new MessageEncoder())
                                    // read timeout value from config file
                                    .addLast("read-timeout-handler",
                                            new ReadTimeoutHandler(PROXY_CONFIG.getTimeoutSeconds()))
                                    // TODO custom heartbeat handler to create or delete application info path
                                    .addLast("HeartbeatHandler", new HeartbeatHandler("proxy-server"))
                                    .addLast("AppInfo2ZkHandler", new AppInfo2ZkHandler())
                                    .addLast("AppInfo2KafkaHandler", new AppInfo2KafkaHandler())
                                    .addLast("SqlInfo2KafkaHandler", new SqlInfo2KafkaHandler())
                                    .addLast("RequestInfo2KafkaHandler", new RequestInfo2KafkaHandler())
                                    .addLast("OtherInfoHandler", new OtherInfoHandler());
                        }
                    })
                    .bind(port).sync()
                    .channel().closeFuture().sync()
            ;
        } catch (Exception e) {
            logger.error("an exception thrown when server starting up", e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public void shutdown() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    public ServerContext getContext() {
        return context;
    }
}

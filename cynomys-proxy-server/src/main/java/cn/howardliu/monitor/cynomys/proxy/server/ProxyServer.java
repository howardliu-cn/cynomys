package cn.howardliu.monitor.cynomys.proxy.server;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.gear.zk.ZkClientFactoryBuilder;
import cn.howardliu.gear.zk.ZkConfig;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.handler.SimpleHeartbeatHandler;
import cn.howardliu.monitor.cynomys.proxy.ServerContext;
import cn.howardliu.monitor.cynomys.proxy.net.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

import static cn.howardliu.monitor.cynomys.proxy.config.ProxyConfig.PROXY_CONFIG;
import static cn.howardliu.monitor.cynomys.proxy.config.SystemSetting.SYSTEM_SETTING;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProxyServer extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private final KafkaProducerWrapper<String, String> kafkaProducerWrapper;
    private final CuratorFramework zkClient;
    private EventLoopGroup bossGroup = new NioEventLoopGroup();
    private EventLoopGroup workerGroup = new NioEventLoopGroup();
    private ServerContext context;

    public ProxyServer() {
        this(PROXY_CONFIG.getPort(), PROXY_CONFIG.getCport());
    }

    public ProxyServer(int port, int cport) {
        super(port, cport);

        Properties config = new Properties();
        config.put(BOOTSTRAP_SERVERS_CONFIG, SYSTEM_SETTING.getKafkaBootstrapServers());
        config.put(ACKS_CONFIG, SYSTEM_SETTING.getKafkaAcks());
        config.put(RETRIES_CONFIG, SYSTEM_SETTING.getKafkaRetries());
        config.put(BATCH_SIZE_CONFIG, SYSTEM_SETTING.getKafkaBatchSize());
        config.put(MAX_REQUEST_SIZE_CONFIG, SYSTEM_SETTING.getKafkaMaxRequestSize());
        this.kafkaProducerWrapper = new KafkaProducerWrapper<>(config);

        this.zkClient = new ZkClientFactoryBuilder()
                .zkAddresses(SYSTEM_SETTING.getZkAddresses())
                .namespace(SYSTEM_SETTING.getZkNamespace())
                .config(ZkConfig.JuteMaxBuffer.key, 10 * 1024 * 1024)
                .build()
                .createClient();
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
                                    .addLast("read-timeout-handler",
                                            new ReadTimeoutHandler(PROXY_CONFIG.getTimeoutSeconds()))
                                    .addLast(new WriteTimeoutHandler(PROXY_CONFIG.getTimeoutSeconds()))
                                    // read maxFrameLength from config file
                                    .addLast(new MessageDecoder(PROXY_CONFIG.getMaxFrameLength(), 4, 4))
                                    .addLast(new MessageEncoder())
                                    // link catch handler to create or delete application info path in zookeeper
                                    .addLast(new LinkCatchHandler(zkClient))
                                    // read timeout value from config file
                                    .addLast(new SimpleHeartbeatHandler("proxy-server"))
                                    .addLast(new AppInfo2ZkHandler(zkClient))
                                    .addLast(new AppInfo2KafkaHandler(kafkaProducerWrapper))
                                    .addLast(new SqlInfo2KafkaHandler(kafkaProducerWrapper))
                                    .addLast(new RequestInfo2KafkaHandler(kafkaProducerWrapper))
                                    .addLast(new OtherInfoHandler(){
                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            cause.printStackTrace();
                                        }
                                    });
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

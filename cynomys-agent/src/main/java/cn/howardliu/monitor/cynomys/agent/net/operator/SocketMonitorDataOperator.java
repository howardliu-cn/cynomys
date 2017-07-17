package cn.howardliu.monitor.cynomys.agent.net.operator;

import cn.howardliu.monitor.cynomys.agent.handler.MonitorChecker;
import cn.howardliu.monitor.cynomys.net.handler.CrcCodeCheckHandler;
import cn.howardliu.monitor.cynomys.agent.net.handler.CustomHeartbeatHandler;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SocketMonitorDataOperator extends AbstractMonitorDataOperator {
    private static final Logger logger = LoggerFactory.getLogger(SocketMonitorDataOperator.class);
    private static final AtomicInteger relinkCount = new AtomicInteger();
    private Channel channel;
    private final Object bootstrapLock = new Object();
    private Bootstrap bootstrap;
    private NioEventLoopGroup workGroup = new NioEventLoopGroup(4);

    private String ip;
    private int port;
    private int reconnectDelaySeconds = super.retryDelaySeconds;
    private volatile boolean linked = false;
    private MonitorChecker monitorChecker;

    public SocketMonitorDataOperator(String ip, int port, MonitorChecker monitorChecker) {
        this.ip = ip;
        this.port = port;
        this.monitorChecker = monitorChecker;
    }

    @Override
    public void start() {
        synchronized (bootstrapLock) {
            bootstrap = new Bootstrap();
            bootstrap.group(workGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                                 @Override
                                 protected void initChannel(SocketChannel ch) throws Exception {
                                     ch.pipeline()
                                             .addLast("idle-state-handler", new IdleStateHandler(0, 0, timeout))
                                             .addLast("MessageDecoder", new MessageDecoder(1024 * 1024 * 100, 4, 4))
                                             .addLast("MessageEncoder", new MessageEncoder())
                                             .addLast("CrcCodeCheckHandler", new CrcCodeCheckHandler())
                                             .addLast("CustomHeartbeatHandler",
                                                     new CustomHeartbeatHandler(getName(), SocketMonitorDataOperator.this))
                                             .addLast("OtherInfoHandler", new OtherInfoHandler());
                                 }
                             }
                    );
        }
    }

    public void reconnect(String ip, int port) {
        synchronized (bootstrapLock) {
            relinkCount.set(0);
            bootstrap = null;
            this.ip = ip;
            this.port = port;
        }
    }

    public boolean connect() throws InterruptedException {
        synchronized (bootstrapLock) {
            if (this.channel != null && this.channel.isActive()) {
                return false;
            }
            bootstrap.connect(this.ip, port)
                    .addListener(new ChannelFutureListener() {
                        @Override
                        public void operationComplete(ChannelFuture future) throws Exception {
                            if (future.isSuccess()) {
                                if (logger.isDebugEnabled()) {
                                    logger.debug("Connect to server [{}:{}] successfully!", ip, port);
                                }
                                channel = future.channel();
                                linked = true;
                            } else {
                                linked = false;
                                // TODO 重连次数可配置
                                if (relinkCount.get() <= 3) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Failed to connect to server [{}:{}], try connect after {}s, this is {} times",
                                                ip, port, reconnectDelaySeconds, relinkCount.get());
                                    }
                                    future.channel().eventLoop().schedule(
                                            new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        relinkCount.incrementAndGet();
                                                        connect();
                                                    } catch (InterruptedException ignored) {
                                                    }
                                                }
                                            },
                                            reconnectDelaySeconds,
                                            TimeUnit.SECONDS
                                    );
                                } else {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug(
                                                "Failed to connect to server [{}:{}], and reconnect {} times, try close and get server list",
                                                ip, port, relinkCount.get());
                                    }
                                    future.channel().close();
                                    // TODO 重新获取地址连接
                                    TimeUnit.SECONDS.sleep(1);
                                    monitorChecker.link();
                                }
                            }
                        }
                    }).sync()
                    .channel().closeFuture().sync();
            return false;
        }
    }

    @Override
    public void handleException(Throwable cause, Message message) {
        if (isActive()) {
            sendData(message);
        } else {
            // TODO write message to file
            monitorChecker.getM().interrupt();
            monitorChecker.startHealth("Active");
        }
    }

    @Override
    public boolean isActive() {
        return this.channel != null && this.channel.isActive() && linked;
    }

    @Override
    public void sendData(Message message) {
        if (linked) {
            try {
                channel.writeAndFlush(message);
            } catch (Throwable t) {
                handleException(t, message);
            }
        }
    }

    @Override
    public void handleData(Message message) {
        try {
            logger.debug("got one message: {}" + message);
        } catch (Exception e) {
            logger.warn("cannot handle data, abort it");
        }
    }

    @Override
    public String getName() {
        return super.getName() == null ? Constant.THIS_TAG : super.getName();
    }
}

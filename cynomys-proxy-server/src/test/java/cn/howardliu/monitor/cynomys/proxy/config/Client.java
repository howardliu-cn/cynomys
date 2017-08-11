package cn.howardliu.monitor.cynomys.proxy.config;

import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.handler.SimpleHeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * <br>created at 17-8-3
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);
    private final EventLoopGroup eventLoopGroupWorker;
    private final Bootstrap bootstrap = new Bootstrap();
    private final ConcurrentMap<String, ChannelWrapper> channelTables = new ConcurrentHashMap<>();
    private final Lock lockChannelTables = new ReentrantLock();

    private final AtomicInteger relinkCount = new AtomicInteger();

    private final AtomicReference<List<String>> addressList = new AtomicReference<>();
    private final AtomicReference<String> addressChossed = new AtomicReference<>();
    private final Lock lockAddressChannel = new ReentrantLock();

    private DefaultEventExecutorGroup defaultEventExecutorGroup;

    Client() {
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

    public static void main(String[] args) throws InterruptedException {
        Client client = new Client();
        client.startup();
        client.updateAddressList(Collections.singletonList("127.0.0.1:7911"));
        client.async(
                new Message()
                        .setHeader(
                                new Header()
                                        .setSysCode("001")
                                        .setSysName("test-client")
                                        .setType(MessageType.HEARTBEAT_REQ.value())
                        )
        );

        TimeUnit.MINUTES.sleep(10);
    }

    void startup() {
        int nThreads = 4;
        this.defaultEventExecutorGroup = new DefaultEventExecutorGroup(
                nThreads,
                new ThreadFactory() {
                    private AtomicInteger threadIndex = new AtomicInteger(0);

                    @Override
                    public Thread newThread(Runnable r) {
                        return new Thread(r, "NettyClientWorkerThread-" + threadIndex.incrementAndGet());
                    }
                }
        );

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
                                .addLast(defaultEventExecutorGroup)
                                .addLast(new ReadTimeoutHandler(60))
                                .addLast(new IdleStateHandler(0, 0, 5))
                                .addLast(new MessageDecoder(10 * 1024 * 1024, 4, 4))
                                .addLast(new MessageEncoder())
                                .addLast(new SimpleHeartbeatHandler("test-client") {
                                    @Override
                                    protected Header customHeader() {
                                        return super.customHeader()
                                                .setSysName(Constant.SYS_NAME)
                                                .setSysCode(Constant.SYS_CODE);
                                    }
                                })
                                .addLast(new NettyConnectManageHandler())
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

    public void shutdown() {
        try {
            for (ChannelWrapper cw : this.channelTables.values()) {
                this.closeChannel(null, cw.getChannel());
            }
            this.channelTables.clear();
            this.eventLoopGroupWorker.shutdownGracefully();
            if (this.defaultEventExecutorGroup != null) {
                this.defaultEventExecutorGroup.shutdownGracefully();
            }
        } catch (Exception e) {
            logger.error("Netty-Client shutdown exception, ", e);
        }
    }

    void updateAddressList(List<String> addresses) {
        List<String> old = this.addressList.get();
        boolean update = false;
        if (!addresses.isEmpty()) {
            if (old == null) {
                update = true;
            } else if (addresses.size() != old.size()) {
                update = true;
            } else {
                for (int i = 0; i < addresses.size() && !update; i++) {
                    if (!old.contains(addresses.get(i))) {
                        update = true;
                    }
                }
            }

            if (update) {
                Collections.shuffle(addresses);
                this.addressList.set(addresses);
            }
        }
    }

    boolean async(Message message) throws InterruptedException {
        final Channel channel = this.getAndCreateUseAddressChoose();
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(message);
            return true;
        }
        return false;
    }

    private Channel getAndCreateChannel(final String address) throws InterruptedException {
        if (address == null) {
            return getAndCreateUseAddressChoose();
        }

        ChannelWrapper cw = this.channelTables.get(address);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }
        return this.createChannel(address);
    }

    private Channel getAndCreateUseAddressChoose() throws InterruptedException {
        String address = this.addressChossed.get();
        if (address != null) {
            ChannelWrapper cw = this.channelTables.get(address);
            if (cw != null && cw.isOK()) {
                return cw.getChannel();
            }
        }
        final List<String> addresses = this.addressList.get();
        if (this.lockAddressChannel.tryLock(3000, TimeUnit.MILLISECONDS)) {
            try {
                address = this.addressChossed.get();
                if (address != null) {
                    ChannelWrapper cw = this.channelTables.get(address);
                    if (cw != null && cw.isOK()) {
                        return cw.getChannel();
                    }
                }

                if (addresses != null && !addresses.isEmpty()) {
                    // TODO no loop
                    String _address = addresses.get(ThreadLocalRandom.current().nextInt(10) % addresses.size());
                    this.addressChossed.set(_address);
                    Channel _channel = this.createChannel(_address);
                    if (_channel != null) {
                        return _channel;
                    }
                }
            } catch (Exception e) {
                logger.error("getAndCreateUseAddressChoose: create server channel exception", e);
            }
        } else {
            logger.warn("getAndCreateUseAddressChoose: try to lock server, but timeout, {}ms", 3000);
        }
        return null;
    }

    private Channel createChannel(final String address) throws InterruptedException {
        ChannelWrapper cw = this.channelTables.get(address);
        if (cw != null && cw.isOK()) {
            return cw.getChannel();
        }

        if (this.lockChannelTables.tryLock(30_000, TimeUnit.MILLISECONDS)) {
            try {
                boolean createNewConnection;
                cw = this.channelTables.get(address);
                if (cw == null) {
                    createNewConnection = true;
                } else {
                    if (cw.isOK()) {
                        return cw.getChannel();
                    } else if (!cw.getChannelFuture().isDone()) {
                        createNewConnection = false;
                    } else {
                        this.channelTables.remove(address);
                        createNewConnection = true;
                    }
                }

                if (createNewConnection) {
                    ChannelFuture channelFuture = this.bootstrap.connect(NetHelper.string2SocketAddress(address))
                            .addListener((ChannelFutureListener) future -> {
                                if (future.isSuccess()) {
                                    if (logger.isDebugEnabled()) {
                                        logger.debug("Connect to server [{}] successfully!", address);
                                    }
                                } else {
                                    if (relinkCount.get() <= 3) {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    "Failed to connect to server [{}], try connect after {}s, this is {} times",
                                                    address, 5000, relinkCount.get());
                                        }
                                        future.channel().eventLoop().schedule(
                                                () -> {
                                                    try {
                                                        relinkCount.incrementAndGet();
                                                        getAndCreateChannel(address);
                                                    } catch (InterruptedException ignored) {
                                                    }
                                                },
                                                5_000,
                                                TimeUnit.MILLISECONDS
                                        );
                                    } else {
                                        if (logger.isDebugEnabled()) {
                                            logger.debug(
                                                    "Failed to connect to server [{}], and reconnect {} times, try close and get server list",
                                                    address, relinkCount.get());
                                        }
                                        this.addressChossed.set(null);
                                        relinkCount.set(0);
                                        closeChannel(future.channel());
                                    }
                                }
                            });
                    logger.info("createChannel: begin to connect remote {} asynchronously", address);
                    cw = new ChannelWrapper(channelFuture);
                    this.channelTables.put(address, cw);
                }
            } catch (Exception e) {
                logger.error("createChannel: create channel exception", e);
            } finally {
                this.lockChannelTables.unlock();
            }
        } else {
            logger.warn("createChannel: try to lock channel table, but timeout, {}ms", 30_000);
        }

        if (cw != null) {
            ChannelFuture channelFuture = cw.getChannelFuture();
            if (channelFuture.awaitUninterruptibly(3000)) {
                if (cw.isOK()) {
                    logger.info("createChannel: connect remote {} success, {}",
                            address, channelFuture.toString());
                    return cw.getChannel();
                } else {
                    logger.warn("createChannel: connect remote {} failed, {}",
                            address, channelFuture.toString(), channelFuture.cause());
                }
            } else {
                logger.warn("createChannel: connect remote {} timeout {}ms, {}",
                        address, 3000, channelFuture.toString());
            }
        }

        return null;
    }

    private void closeChannel(final Channel channel) {
        if (channel == null) {
            return;
        }
        try {
            if (this.lockChannelTables.tryLock(3000, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    ChannelWrapper wrapper = null;
                    String address = null;
                    for (Map.Entry<String, ChannelWrapper> entry : this.channelTables.entrySet()) {
                        ChannelWrapper cw = entry.getValue();
                        if (cw != null && cw.getChannel() == channel) {
                            address = entry.getKey();
                            wrapper = cw;
                            break;
                        }
                    }

                    if (wrapper == null) {
                        logger.info("eventCloseChannel: the channel has been removed from the channel table before");
                        removeItemFromTable = false;
                    } else if (wrapper.getChannel() != channel) {
                        logger.info(
                                "closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                                address);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(address);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", address);
                        NetHelper.closeChannel(channel);
                    }

                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", 3000);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }

    void closeChannel(final String remote, final Channel channel) {
        if (channel == null) {
            return;
        }
        final String address = remote == null ? NetHelper.remoteAddress(channel) : remote;
        try {
            if (this.lockChannelTables.tryLock(3000, TimeUnit.MILLISECONDS)) {
                try {
                    boolean removeItemFromTable = true;
                    ChannelWrapper wrapper = this.channelTables.get(address);

                    logger.info("closeChannel: begin close the channel[{}] Found: {}", remote, wrapper != null);

                    if (wrapper == null) {
                        logger.info("eventCloseChannel: the channel has been removed from the channel table before");
                        removeItemFromTable = false;
                    } else if (wrapper.getChannel() != channel) {
                        logger.info(
                                "closeChannel: the channel[{}] has been closed before, and has been created again, nothing to do.",
                                address);
                        removeItemFromTable = false;
                    }

                    if (removeItemFromTable) {
                        this.channelTables.remove(address);
                        logger.info("closeChannel: the channel[{}] was removed from channel table", address);
                    }

                    NetHelper.closeChannel(channel);
                } catch (Exception e) {
                    logger.error("closeChannel: close the channel exception", e);
                } finally {
                    this.lockChannelTables.unlock();
                }
            } else {
                logger.warn("closeChannel: try to lock channel table, but timeout, {}ms", 3000);
            }
        } catch (InterruptedException e) {
            logger.error("closeChannel exception", e);
        }
    }

    static class ChannelWrapper {
        private final ChannelFuture channelFuture;

        ChannelWrapper(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        boolean isOK() {
            return this.channelFuture.channel() != null && this.channelFuture.channel().isActive();
        }

        public boolean isWriteable() {
            return this.channelFuture.channel().isWritable();
        }

        private Channel getChannel() {
            return this.channelFuture.channel();
        }

        ChannelFuture getChannelFuture() {
            return channelFuture;
        }
    }

    class NettyConnectManageHandler extends ChannelDuplexHandler {
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY CLIENT PIPELINE: channelInactive {}", remoteAddress);
            closeChannel(ctx.channel());
            super.channelInactive(ctx);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                ChannelPromise promise) throws Exception {
            final String local = localAddress == null ? "UNKNOWN" : localAddress.toString();
            final String remote = remoteAddress == null ? "UNKNOWN" : remoteAddress.toString();
            logger.info("NETTY CLIENT PIPELINE: CONNECT  {} => {}", local, remote);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY CLIENT PIPELINE: DISCONNECT {}", remoteAddress);
            closeChannel(ctx.channel());
            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.info("NETTY CLIENT PIPELINE: CLOSE {}", remoteAddress);
            closeChannel(ctx.channel());
            super.close(ctx, promise);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            final String remoteAddress = NetHelper.remoteAddress(ctx.channel());
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught {}", remoteAddress);
            logger.warn("NETTY CLIENT PIPELINE: exceptionCaught exception.", cause);
            closeChannel(ctx.channel());
        }
    }
}

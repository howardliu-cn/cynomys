package cn.howardliu.monitor.cynomys.proxy.server;

import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.handler.OtherInfoHandler;
import cn.howardliu.monitor.cynomys.net.handler.SimpleHeartbeatHandler;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-3
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Client {
    private static final Logger logger = LoggerFactory.getLogger(Client.class);

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
                                    .addLast(new ReadTimeoutHandler(60))
                                    .addLast(new IdleStateHandler(0, 0, 5))
                                    // read maxFrameLength from config file
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
                                    .addLast(new OtherInfoHandler() {
                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
                                                throws Exception {
                                            cause.printStackTrace();
                                        }
                                    })
                            ;
                        }
                    })
                    .connect("127.0.0.1", 7911)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            logger.debug("Connect to server successfully!");
                        } else {
                            logger.debug("Failed to connect to server!");
                        }
                    })
                    .sync()
                    .channel().closeFuture().sync()
            ;
        } finally {
            workGroup.shutdownGracefully();
        }
    }
}

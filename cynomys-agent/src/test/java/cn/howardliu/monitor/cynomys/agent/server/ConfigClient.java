package cn.howardliu.monitor.cynomys.agent.server;

import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.apache.commons.lang3.StringUtils;

/**
 * <br>created at 17-5-22
 *
 * @author liuxh
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigClient {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("MessageDecoder", new MessageDecoder(1024 * 1024 * 100, 4, 4))
                                    .addLast("MessageEncoder", new MessageEncoder())
                                    .addLast("read-timeout-handler", new ReadTimeoutHandler(50))
                                    .addLast(new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            query(ctx);
                                        }

                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg)
                                                throws Exception {
                                            if (msg == null || msg.getHeader() == null || msg.getBody() == null) {
                                                ctx.fireChannelRead(msg);
                                                return;
                                            }
                                            if (MessageCode.CONFIG_RESP.value() == msg.getHeader().getCode()) {
                                                if (StringUtils.isBlank(msg.getBody())) {
                                                    query(ctx);
                                                }
                                                try {
                                                    System.out.println("Server list is: " + msg.getBody());
                                                    ctx.close().addListener(ChannelFutureListener.CLOSE);
                                                } catch (Exception e) {
                                                    query(ctx);
                                                }
                                            } else {
                                                ctx.fireChannelRead(msg);
                                            }
                                        }

                                        private void query(ChannelHandlerContext ctx) {
                                            ctx.writeAndFlush(new Message().setHeader(new Header().setType(MessageCode.CONFIG_REQ.value())));
                                        }
                                    });
                        }
                    })
                    .connect("127.0.0.1", 8081).sync()
                    .channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}

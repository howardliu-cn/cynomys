package cn.howardliu.monitor.cynomys.agent.server;

import cn.howardliu.monitor.cynomys.agent.net.ServerInfo;
import cn.howardliu.monitor.cynomys.net.codec.MessageDecoder;
import cn.howardliu.monitor.cynomys.net.codec.MessageEncoder;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.Collections;

import static cn.howardliu.monitor.cynomys.agent.net.ServerInfo.ServerType.LAN;

/**
 * <br>created at 17-5-22
 *
 * @author liuxh
 * @version 1.0.0
 * @since 1.0.0
 */
public class ConfigServer {
    public static void main(String[] args) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast("MessageDecoder", new MessageDecoder(1024 * 1024 * 100, 4, 4))
                                    .addLast("MessageEncoder", new MessageEncoder())
                                    .addLast("read-timeout-handler", new ReadTimeoutHandler(50))
                                    .addLast(new SimpleChannelInboundHandler<Message>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, Message msg)
                                                throws Exception {
                                            if (msg != null && msg.getHeader() != null) {
                                                if (MessageCode.CONFIG_REQ.value() == msg.getHeader().getCode()) {
                                                    String list = JSON.toJSONString(
                                                            Collections.singletonList(
                                                                    new ServerInfo(
                                                                            "127.0.0.1",
                                                                            8082,
                                                                            LAN
                                                                    )
                                                            )
                                                    );
                                                    ctx.writeAndFlush(
                                                            new Message()
                                                                    .setHeader(
                                                                            new Header()
                                                                                    .setType(MessageType.RESPONSE.value())
                                                                                    .setCode(MessageCode.CONFIG_RESP
                                                                                            .value())
                                                                                    .setLength(list.length()))
                                                                    .setBody(list)
                                                    );
                                                } else {
                                                    ctx.fireChannelRead(msg);
                                                }
                                            }
                                        }
                                    })
                            ;
                        }
                    })
                    .bind(8081).sync()
                    .channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

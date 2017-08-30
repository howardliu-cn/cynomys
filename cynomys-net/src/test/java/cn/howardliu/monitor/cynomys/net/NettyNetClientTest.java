package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import cn.howardliu.monitor.cynomys.net.netty.NettyNetClient;
import cn.howardliu.monitor.cynomys.net.netty.NettyNetServer;
import cn.howardliu.monitor.cynomys.net.netty.NettyServerConfig;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NettyNetClientTest {
    private static NetClient netClient;
    private static NetServer netServer;

    @Before
    public void setUp() throws Exception {
        netClient = new NettyNetClient(new NettyClientConfig());
        netClient.updateAddressList(Collections.singletonList("127.0.0.1:7911"));
        netClient.start();

        netServer = new NettyNetServer(new NettyServerConfig()) {
            @Override
            protected ChannelHandler[] additionalChannelHandler2() {
                return new ChannelHandler[]{
                        new SimpleChannelInboundHandler<Message>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, Message message)
                                    throws Exception {
                                System.out.println(message.getBody());
                            }
                        }
                };
            }
        };
        netServer.start();
    }

    @After
    public void tearDown() throws Exception {
        netClient.shutdown();
        netServer.shutdown();
    }

    @Test
    public void async() throws Exception {
        netClient.sync(
                new Message()
                        .setHeader(
                                new Header()
                                        .setSysCode("001")
                                        .setSysName("test-client")
                                        .setType(MessageType.REQUEST.value())
                                        .setCode(MessageCode.HEARTBEAT_REQ.value())
                        )
                        .setBody(
                                "{\"bizCode\":\"050\",\"bizDesc\":\"系统异常\",\"createDate\":\"2017-08-29 16:26:54\",\"errCode\":\"903\",\"errDesc\":\"Could not read JSON: Unrecognized field \\\"saleNo\\\" (class com.wangfj.sales.core.controller.online.support.RefundApplyPara), not marked as ignorable (34 known properties: , \\\"paymentClassDesc\\\", \\\"channel\\\", \\\"exchangeNo\\\", \\\"originalDeliveryNo\\\", \\\"refundStatus\\\", \\\"latestUpdateMan\\\", \\\"sid\\\", \\\"accountNo\\\", \\\"problemDesc\\\", \\\"refundType\\\", \\\"paymentClass\\\", \\\"createdTime\\\" [truncated]])\\n at [Source: com.wfj.netty.server.servlet.DelegatingServletInputStream@542e776a; line: 1, column: 56] (through reference chain: com.wangfj.sales.core.controller.online.support.RefundApplyPara[\\\"saleNo\\\"]); nested exception is com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field \\\"saleNo\\\" (class com.wangfj.sales.core.controller.online.support.RefundApplyPara), not marked as ignorable (34 known properties: , \\\"paymentClassDesc\\\", \\\"channel\\\", \\\"exchangeNo\\\", \\\"originalDeliveryNo\\\", \\\"refundStatus\\\", \\\"latestUpdateMan\\\", \\\"sid\\\", \\\"accountNo\\\", \\\"problemDesc\\\", \\\"refundType\\\", \\\"paymentClass\\\", \\\"createdTime\\\" [truncated]])\\n at [Source: com.wfj.netty.server.servlet.DelegatingServletInputStream@542e776a; line: 1, column: 56] (through reference chain: com.wangfj.sales.core.controller.online.support.RefundApplyPara[\\\"saleNo\\\"])\",\"errId\":\"1020050903230201708291626543360725\",\"errLevel\":\"1\",\"flag\":\"0\",\"processStatus\":\"0\",\"sysCode\":\"20\",\"sysErrCode\":\"230\",\"sysErrDesc\":\"其他异常\",\"throwableDesc\":\"org.springframework.http.converter.HttpMessageNotReadableException:Could not read JSON: Unrecognized field \\\"saleNo\\\" (class com.wangfj.sales.core.controller.online.support.RefundApplyPara), not marked as ignorable (34 known properties: , \\\"paymentClassDesc\\\", \\\"channel\\\", \\\"exchangeNo\\\", \\\"originalDeliveryNo\\\", \\\"refundStatus\\\", \\\"latestUpdateMan\\\", \\\"sid\\\", \\\"accountNo\\\", \\\"problemDesc\\\", \\\"refundType\\\", \\\"paymentClass\\\", \\\"createdTime\\\" [truncated]])\\n at [Source: com.wfj.netty.server.servlet.DelegatingServletInputStream@542e776a; line: 1, column: 56] (through reference chain: com.wangfj.sales.core.controller.online.support.RefundApplyPara[\\\"saleNo\\\"]); nested exception is com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException: Unrecognized field \\\"saleNo\\\" (class com.wangfj.sales.core.controller.online.support.RefundApplyPara), not marked as ignorable (34 known properties: , \\\"paymentClassDesc\\\", \\\"channel\\\", \\\"exchangeNo\\\", \\\"originalDeliveryNo\\\", \\\"refundStatus\\\", \\\"latestUpdateMan\\\", \\\"sid\\\", \\\"accountNo\\\", \\\"problemDesc\\\", \\\"refundType\\\", \\\"paymentClass\\\", \\\"createdTime\\\" [truncated]])\\n at [Source: com.wfj.netty.server.servlet.DelegatingServletInputStream@542e776a; line: 1, column: 56] (through reference chain: com.wangfj.sales.core.controller.online.support.RefundApplyPara[\\\"saleNo\\\"])\\norg.springframework.http.converter.json.MappingJackson2HttpMessageConverter.readJavaType(MappingJackson2HttpMessageConverter.java:171)\\norg.springframework.http.converter.json.MappingJackson2HttpMessageConverter.read(MappingJackson2HttpMessageConverter.java:163)\\norg.springframework.web.servlet.mvc.method.annotation.AbstractMessageConverterMethodArgumentResolver.readWithMessageConverters(AbstractMessageConverterMethodArgumentResolver.java:135)\\norg.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor.readWithMessageConverters(RequestResponseBodyMethodProcessor.java:180)\\norg.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor.resolveArgument(RequestResponseBodyMethodProcessor.java:95)\\norg.springframework.web.method.support.HandlerMethodArgumentResolverComposite.resolveArgument(HandlerMethodArgumentResolverComposite.java:77)\\norg.springframework.web.method.support.InvocableHandlerMethod.getMethodArgumentValues(InvocableHandlerMethod.java:162)\\norg.springframework.web.method.support.InvocableHandlerMethod.invokeForRequest(InvocableHandlerMethod.java:123)\\norg.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod.invokeAndHandle(ServletInvocableHandlerMethod.java:104)\\norg.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.invokeHandleMethod(RequestMappingHandlerAdapter.java:745)\\norg.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(RequestMappingHandlerAdapter.java:686)\\norg.springframework.web.servlet.mvc.method.AbstractHandlerMethodAdapter.handle(AbstractHandlerMethodAdapter.java:80)\\norg.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:925)\\norg.springframework.web.servlet.DispatcherServlet.doService(DispatcherServlet.java:856)\\norg.springframework.web.servlet.FrameworkServlet.processRequest(FrameworkServlet.java:936)\\norg.springframework.web.servlet.FrameworkServlet.doPost(FrameworkServlet.java:838)\\njavax.servlet.http.HttpServlet.service(HttpServlet.java:707)\\norg.springframework.web.servlet.FrameworkServlet.service(FrameworkServlet.java:812)\\njavax.servlet.http.HttpServlet.service(HttpServlet.java:790)\\ncom.wfj.netty.server.servlet.WFJFilterChain.internalDoFilter(WFJFilterChain.java:98)\\ncom.wfj.netty.server.servlet.WFJFilterChain.doFilter(WFJFilterChain.java:69)\\ncom.wfj.netty.servlet.filter.Monitor.doFilter(Monitor.java:100)\\ncom.wfj.netty.server.servlet.WFJFilterChain.internalDoFilter(WFJFilterChain.java:80)\\ncom.wfj.netty.server.servlet.WFJFilterChain.doFilter(WFJFilterChain.java:69)\\ncom.wfj.netty.server.container.handler.HTTPHandler.doService(HTTPHandler.java:187)\\ncom.wfj.netty.server.container.handler.HTTPHandler.channelRead0(HTTPHandler.java:116)\\ncom.wfj.netty.server.container.handler.HTTPHandler.channelRead0(HTTPHandler.java:68)\\nio.netty.channel.SimpleChannelInboundHandler.channelRead(SimpleChannelInboundHandler.java:105)\\nio.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:339)\\nio.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:324)\\nio.netty.channel.ChannelInboundHandlerAdapter.channelRead(ChannelInboundHandlerAdapter.java:86)\\nio.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:339)\\nio.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:324)\\nio.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)\\nio.netty.handler.codec.MessageToMessageCodec.channelRead(MessageToMessageCodec.java:111)\\nio.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:339)\\nio.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:324)\\nio.netty.handler.codec.MessageToMessageDecoder.channelRead(MessageToMessageDecoder.java:103)\\nio.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:339)\\nio.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:324)\\nio.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:242)\\nio.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:339)\\nio.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:324)\\nio.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:847)\\nio.netty.channel.nio.AbstractNioByteChannel$NioByteUnsafe.read(AbstractNioByteChannel.java:131)\\nio.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:511)\\nio.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:468)\\nio.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:382)\\nio.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:354)\\nio.netty.util.concurrent.SingleThreadEventExecutor$2.run(SingleThreadEventExecutor.java:111)\\nio.netty.util.concurrent.DefaultThreadFactory$DefaultRunnableDecorator.run(DefaultThreadFactory.java:137)\\njava.lang.Thread.run(Thread.java:722)\"}")
        );

        TimeUnit.MINUTES.sleep(60);
    }
}
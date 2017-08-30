package cn.howardliu.monitor.cynomys.client.common;

import cn.howardliu.monitor.cynomys.common.CynomysVersion;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import cn.howardliu.monitor.cynomys.net.InvokeCallBack;
import cn.howardliu.monitor.cynomys.net.NetClient;
import cn.howardliu.monitor.cynomys.net.exception.NetConnectException;
import cn.howardliu.monitor.cynomys.net.exception.NetSendRequestException;
import cn.howardliu.monitor.cynomys.net.exception.NetTimeoutException;
import cn.howardliu.monitor.cynomys.net.exception.NetTooMuchRequestException;
import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import cn.howardliu.monitor.cynomys.net.netty.NettyNetClient;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static cn.howardliu.monitor.cynomys.common.Constant.VERSION_KEY;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class CynomysClient implements NetClient {
    private static final Logger logger = LoggerFactory.getLogger(CynomysClient.class);

    static {
        System.setProperty(VERSION_KEY, Integer.toString(CynomysVersion.CURRENT_VERSION));
    }

    private final NettyClientConfig nettyClientConfig;
    private final NetClient netClient;

    public CynomysClient(final NettyClientConfig nettyClientConfig, final ChannelEventListener channelEventListener) {
        this.nettyClientConfig = nettyClientConfig;
        this.netClient = new NettyNetClient(nettyClientConfig, channelEventListener) {
            @Override
            protected ChannelFutureListener connectListener(final String address) {
                return new ChannelFutureListener() {
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if (future.isSuccess()) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Connect to server [{}] successfully!", address);
                            }
                        } else {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Failed to connect to server [{}]!", address);
                            }
                        }
                    }
                };
            }
        };
    }

    public List<String> getAddressList() {
        return this.netClient.getAddressList();
    }


    public NetClient getNetClient() {
        return netClient;
    }

    public void updateAddressList(final String addresses) {
        this.updateAddressList(Arrays.asList(addresses.split(",")));
    }

    public void updateAddressList(final List<String> addresses) {
        this.netClient.updateAddressList(addresses);
    }

    public void start() {
        this.netClient.start();
    }

    public void shutdown() {
        this.netClient.shutdown();
    }

    @Override
    public boolean isStopped() {
        return this.netClient.isStopped();
    }

    @Override
    public boolean isStarted() {
        return this.netClient.isStarted();
    }

    @Override
    public void connect() throws InterruptedException {
        this.netClient.connect();
    }

    @Override
    public void connect(String address) throws InterruptedException {
        this.netClient.connect(address);
    }

    @Override
    public boolean isChannelWriteable(String address) {
        return this.netClient.isChannelWriteable(address);
    }

    @Override
    public Message sync(Message request)
            throws InterruptedException, NetConnectException, NetTimeoutException, NetSendRequestException {
        if (this.netClient.isStopped()) {
            logger.warn("the NetClient is STOPPED!");
            return null;
        }
        return this.netClient.sync(request);
    }

    @Override
    public Message sync(Message request, long timeoutMillis)
            throws InterruptedException, NetConnectException, NetTimeoutException, NetSendRequestException {
        if (this.netClient.isStopped()) {
            logger.warn("the NetClient is STOPPED!");
            return null;
        }
        return this.netClient.sync(request, timeoutMillis);
    }

    @Override
    public void async(Message request, InvokeCallBack invokeCallBack)
            throws InterruptedException, NetConnectException, NetTooMuchRequestException, NetSendRequestException,
            NetTimeoutException {
        if (this.netClient.isStopped()) {
            logger.warn("the NetClient is STOPPED!");
            return;
        }
        this.netClient.async(request, invokeCallBack);
    }

    @Override
    public void async(Message request, long timeoutMills, InvokeCallBack invokeCallBack)
            throws InterruptedException, NetConnectException, NetTooMuchRequestException, NetSendRequestException,
            NetTimeoutException {
        if (this.netClient.isStopped()) {
            logger.warn("the NetClient is STOPPED!");
            return;
        }
        this.netClient.async(request, timeoutMills, invokeCallBack);
    }

    public NettyClientConfig getNettyClientConfig() {
        return nettyClientConfig;
    }
}

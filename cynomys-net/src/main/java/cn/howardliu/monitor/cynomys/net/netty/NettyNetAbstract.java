package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.common.ServiceThread;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.exception.NetConnectException;
import cn.howardliu.monitor.cynomys.net.exception.NetSendRequestException;
import cn.howardliu.monitor.cynomys.net.exception.NetTimeoutException;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public abstract class NettyNetAbstract {
    private static final Logger logger = LoggerFactory.getLogger(NettyNetAbstract.class);
    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor();
    protected final ConcurrentMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);

    public abstract ChannelEventListener getChannelEventListener();

    public Message invokeSync(final Channel channel, final Message message, final long timeoutMillis)
            throws InterruptedException, NetConnectException, NetTimeoutException, NetSendRequestException {
        final int opaque = message.getHeader().getOpaque();
        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (!future.isSuccess()) {
                    responseFuture.setSendRequestOK(false);
                }
                responseTable.remove(opaque);
                responseFuture.setCause(future.cause());
                logger.warn("send a request to channel <{}> failed!", NetHelper.remoteAddress(channel));
            });
            Message response = responseFuture.waitResponse();
            if (response == null) {
                if (responseFuture.isSendRequestOK()) {
                    throw new NetTimeoutException(NetHelper.remoteAddress(channel), timeoutMillis,
                            responseFuture.getCause());
                } else {
                    throw new NetSendRequestException(NetHelper.remoteAddress(channel), responseFuture.getCause());
                }
            }
            return response;
        } finally {
            responseTable.remove(opaque);
        }
    }

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecutor.putNettyEvent(event);
    }

    public void scanResponseTable() {
        final List<ResponseFuture> rfList = new LinkedList<>();

        Iterator<Map.Entry<Integer, ResponseFuture>> it = this.responseTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ResponseFuture> next = it.next();
            ResponseFuture response = next.getValue();
            if (response.getBeginTimestamp() + response.getTimeoutMillis() + 1000 <= System.currentTimeMillis()) {
                response.release();
                it.remove();
                rfList.add(response);
                logger.warn("remove timeout request, " + response);
            }
        }

        for (ResponseFuture rf : rfList) {
            try {
                executeInvokeCallback(rf);
            } catch (Throwable e) {
                logger.warn("scanResponseTable, operationComplete Exception", e);
            }
        }
    }

    abstract public ExecutorService getCallbackExecutor();

    private void executeInvokeCallback(final ResponseFuture responseFuture) {
        boolean runThisThread = false;
        ExecutorService executor = this.getCallbackExecutor();
        if (executor != null) {
            try {
                executor.submit(() -> {
                    try {
                        responseFuture.executeInvokeCallback();
                    } catch (Throwable e) {
                        logger.warn("execute callback in executor exception, and callback throw", e);
                    }
                });
            } catch (Exception e) {
                runThisThread = true;
                logger.warn("execute callback in executor exception, maybe executor busy", e);
            }
        } else {
            runThisThread = true;
        }

        if (runThisThread) {
            try {
                responseFuture.executeInvokeCallback();
            } catch (Throwable e) {
                logger.warn("executeInvokeCallback Exception", e);
            }
        }
    }

    class NettyEventExecutor extends ServiceThread {
        private final LinkedBlockingQueue<NettyEvent> eventQueue = new LinkedBlockingQueue<NettyEvent>();
        private final int maxSize = 10000;

        public void putNettyEvent(final NettyEvent event) {
            if (this.eventQueue.size() <= maxSize) {
                this.eventQueue.add(event);
            } else {
                logger.warn("event queue size[{}] enough, so drop this event {}",
                        this.eventQueue.size(), event.toString());
            }
        }

        @Override
        public void run() {
            logger.info(this.getServiceName() + " service started");

            final ChannelEventListener listener = NettyNetAbstract.this.getChannelEventListener();

            while (!this.isStopped()) {
                try {
                    NettyEvent event = this.eventQueue.poll(3000, TimeUnit.MILLISECONDS);
                    if (event != null && listener != null) {
                        switch (event.getType()) {
                            case IDLE:
                                listener.onChannelIdle(event.getRemoteAddress(), event.getChannel());
                                break;
                            case CLOSE:
                                listener.onChannelClose(event.getRemoteAddress(), event.getChannel());
                                break;
                            case CONNECT:
                                listener.onChannelConnect(event.getRemoteAddress(), event.getChannel());
                                break;
                            case EXCEPTION:
                                listener.onChannelException(event.getRemoteAddress(), event.getChannel(),
                                        event.getCause());
                                break;
                            default:
                                break;
                        }
                    }
                } catch (Exception e) {
                    logger.warn(this.getServiceName() + " service has exception. ", e);
                }
            }
            logger.info(this.getServiceName() + " service end");
        }

        @Override
        public String getServiceName() {
            return NettyEventExecutor.class.getSimpleName();
        }
    }
}

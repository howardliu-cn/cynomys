package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.common.Pair;
import cn.howardliu.monitor.cynomys.common.SemaphoreReleaseOnlyOnce;
import cn.howardliu.monitor.cynomys.common.ServiceThread;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import cn.howardliu.monitor.cynomys.net.InvokeCallBack;
import cn.howardliu.monitor.cynomys.net.NetHelper;
import cn.howardliu.monitor.cynomys.net.exception.NetSendRequestException;
import cn.howardliu.monitor.cynomys.net.exception.NetTimeoutException;
import cn.howardliu.monitor.cynomys.net.exception.NetTooMuchRequestException;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
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
    protected final Semaphore semaphoreAsync;
    protected final NettyEventExecutor nettyEventExecutor = new NettyEventExecutor();
    protected final ConcurrentMap<Integer, ResponseFuture> responseTable = new ConcurrentHashMap<>(256);

    protected final Map<Byte, Pair<NettyRequestProcessor, ExecutorService>> processorTable = new HashMap<>(64);
    protected Pair<NettyRequestProcessor, ExecutorService> defaultRequestProcessor;

    public NettyNetAbstract(final int permitsAsync) {
        this.semaphoreAsync = new Semaphore(permitsAsync, true);
    }

    public abstract ChannelEventListener getChannelEventListener();

    public Message invokeSync(final Channel channel, final Message request, final long timeoutMillis)
            throws InterruptedException, NetTimeoutException, NetSendRequestException {
        final int opaque = request.getHeader().getOpaque();
        try {
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, null, null);
            this.responseTable.put(opaque, responseFuture);
            channel.writeAndFlush(request)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isSuccess()) {
                            responseFuture.setSendRequestOK(true);
                            return;
                        } else {
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

    public void invokeAsync(final Channel channel, final Message request, final long timeoutMillis,
            final InvokeCallBack invokeCallBack)
            throws InterruptedException, NetTooMuchRequestException, NetTimeoutException, NetSendRequestException {
        final int opaque = request.getHeader().getOpaque();
        boolean acquired = this.semaphoreAsync.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
        if (acquired) {
            final SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(this.semaphoreAsync);
            final ResponseFuture responseFuture = new ResponseFuture(opaque, timeoutMillis, invokeCallBack, once);
            this.responseTable.put(opaque, responseFuture);
            try {
                channel.writeAndFlush(request)
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isSuccess()) {
                                responseFuture.setSendRequestOK(true);
                                return;
                            } else {
                                responseFuture.setSendRequestOK(false);
                            }
                            responseFuture.putResponse(null);
                            responseTable.remove(opaque);
                            try {
                                executeInvokeCallback(responseFuture);
                            } catch (Throwable e) {
                                logger.warn("execute callback in writeAndFlush addListener, and callback throw", e);
                            } finally {
                                responseFuture.release();
                            }

                            logger.warn("send a request to channel <{}> failed!", NetHelper.remoteAddress(channel));
                        });
            } catch (Exception e) {
                responseFuture.release();
                String remoteAddress = NetHelper.remoteAddress(channel);
                logger.warn("send a request to channel <{}> exception", remoteAddress, e);
                throw new NetSendRequestException(remoteAddress, e);
            }
        } else {
            String cause = "invokeAsync tryAcquire semaphore timeout, " + timeoutMillis + "ms, "
                    + "waiting thread numbers: " + this.semaphoreAsync.getQueueLength()
                    + " semaphoreAsyncValue: " + this.semaphoreAsync.availablePermits();
            logger.warn(cause);
            throw new NetTooMuchRequestException(cause);
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

    public void processRequest(ChannelHandlerContext ctx, Message request) {
        Pair<NettyRequestProcessor, ExecutorService> matched = this.processorTable.get(request.getHeader().getCode());
        Pair<NettyRequestProcessor, ExecutorService> pair = matched == null ? this.defaultRequestProcessor : matched;
        final int opaque = request.getHeader().getOpaque();

        if (pair != null) {
            Runnable run = () -> {
                try {
                    final Message response = pair.getObject1().processRequest(ctx, request);
                    response.getHeader().setOpaque(opaque);
                    response.getHeader().setType(MessageType.RESPONSE.value());
                    ctx.writeAndFlush(request);
                } catch (Throwable e) {
                    ctx.writeAndFlush(
                            new Message()
                                    .setHeader(
                                            new Header()
                                                    .setOpaque(opaque)
                                                    .setType(MessageType.RESPONSE.value())
                                                    .setRemark(NetHelper.exceptionSimpleDesc(e))
                                    )
                    );
                }
            };

            if (pair.getObject1().rejectRequest()) {
                ctx.writeAndFlush(
                        new Message()
                                .setHeader(new Header()
                                        .setOpaque(opaque)
                                        .setType(MessageType.RESPONSE.value())
                                        .setRemark("system busy, start flow control for a while")
                                )
                );
                return;
            }

            try {
                pair.getObject2().submit(new NetTask(run, ctx.channel(), request));
            } catch (RejectedExecutionException e) {
                logger.warn("{}, too many requests and system thread pool busy, {}, request code : {}",
                        NetHelper.remoteAddress(ctx.channel()), pair.getObject2().toString(),
                        request.getHeader().getCode());

                ctx.writeAndFlush(
                        new Message()
                                .setHeader(new Header()
                                        .setOpaque(opaque)
                                        .setType(MessageType.RESPONSE.value())
                                        .setRemark("system busy, start flow control for a while")
                                )
                );
            }
        } else {
            String error = "request code " + request.getHeader().getCode() + " not supported!";
            ctx.writeAndFlush(
                    new Message()
                            .setHeader(new Header()
                                    .setOpaque(opaque)
                                    .setType(MessageType.RESPONSE.value())
                                    .setRemark(error)
                            )
            );
            logger.warn(NetHelper.remoteAddress(ctx.channel()) + ' ' + error);
        }
    }

    public void processResponse(ChannelHandlerContext ctx, Message response) {
        final int opaque = response.getHeader().getOpaque();
        final ResponseFuture responseFuture = responseTable.get(opaque);
        if (responseFuture != null) {
            responseFuture.setResponse(response);
            responseFuture.release();
            responseTable.remove(opaque);

            if (responseFuture.getInvokeCallBack() != null) {
                executeInvokeCallback(responseFuture);
            } else {
                responseFuture.putResponse(response);
            }
        } else {
            logger.warn("receive response, but not matched any request, remoteAddress: {}, response: {}",
                    NetHelper.remoteAddress(ctx.channel()), response);
        }
    }

    public abstract ExecutorService getCallbackExecutor();

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

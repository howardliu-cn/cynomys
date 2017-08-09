package cn.howardliu.monitor.cynomys.net.netty;

import cn.howardliu.monitor.cynomys.common.ServiceThread;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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

    public abstract ChannelEventListener getChannelEventListener();

    public void putNettyEvent(final NettyEvent event) {
        this.nettyEventExecutor.putNettyEvent(event);
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

package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.netty.NettyRequestProcessor;

import java.util.concurrent.ExecutorService;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public interface NetServer extends NetService {
    int localListenPort();

    void registProcessor(final byte requestCode, final NettyRequestProcessor processor, final ExecutorService executor);

    void registDefaultProcessor(final NettyRequestProcessor processor, final ExecutorService executor);
}

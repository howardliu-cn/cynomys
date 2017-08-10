package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.netty.ResponseFuture;

/**
 * <br>created at 17-8-10
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public interface InvokeCallBack {
    void operationComplete(final ResponseFuture responseFuture);
}

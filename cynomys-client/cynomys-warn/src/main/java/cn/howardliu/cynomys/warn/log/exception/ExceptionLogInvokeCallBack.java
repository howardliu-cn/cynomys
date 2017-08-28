package cn.howardliu.cynomys.warn.log.exception;

import cn.howardliu.monitor.cynomys.net.InvokeCallBack;
import cn.howardliu.monitor.cynomys.net.netty.ResponseFuture;
import cn.howardliu.monitor.cynomys.net.struct.Message;

/**
 * <br>created at 17-8-28
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ExceptionLogInvokeCallBack implements InvokeCallBack {
    private final Message request;

    public ExceptionLogInvokeCallBack(Message request) {
        this.request = request;
    }

    @Override
    public void operationComplete(ResponseFuture responseFuture) {
        if (responseFuture.isSendRequestOK()) {
            return;
        }
        String exceptionLog = request.getBody();
        // TODO write request into sql or other stage
    }
}

package cn.howardliu.cynomys.warn.exception;

import cn.howardliu.monitor.cynomys.net.InvokeCallBack;
import cn.howardliu.monitor.cynomys.net.netty.ResponseFuture;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-28
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ExceptionLogInvokeCallBack implements InvokeCallBack {
    private static final Logger logger = LoggerFactory.getLogger(ExceptionLogInvokeCallBack.class);
    private final Message request;

    public ExceptionLogInvokeCallBack(Message request) {
        this.request = request;
    }

    @Override
    public void operationComplete(ResponseFuture responseFuture) {
        if (responseFuture.isSendRequestOK()) {
            return;
        }
        String exceptionMsg = request.getBody();
        if (exceptionMsg == null) {
            return;
        }
        try {
            ExceptionLog exceptionLog = JSON.parseObject(exceptionMsg, ExceptionLog.class);
            String errId = exceptionLog.getErrId();
            ExceptionLogCaching.INSTANCE.upsert(errId, exceptionMsg);
        } catch (Exception e) {
            logger.error("upsert exception log into Stage exception, Detail: {}", exceptionMsg, e);
        }
    }
}

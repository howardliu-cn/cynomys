package cn.howardliu.cynomys.warn;

import cn.howardliu.cynomys.warn.log.exception.*;
import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum WarnLoggingClient implements Closeable {
    INSTANCE;

    private final Logger logger = LoggerFactory.getLogger(WarnLoggingClient.class);
    private final SysErrorResolver resolver = new SysErrorResolver();
    private final CynomysClient cynomysClient;

    WarnLoggingClient() {
        // FIXME not gracefully, must refactor
        if (Constant.NO_FLAG) {
            SystemPropertyConfig.init();
        } else {
            try {
                LaunchLatch.CLIENT_INIT.waitForMillis(120_000 + 2_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                logger.error("LaunchLatch was interrupted!", e);
            }
        }
        this.cynomysClient = CynomysClientManager.INSTANCE
                .getAndCreateCynomysClient(
                        new ClientConfig(),
                        new SimpleChannelEventListener()
                );
    }

    public void log(String sysCode,
            String bizCode, String bizDesc,
            String errCode, String errDesc,
            String sysErrCode, String sysErrDesc,
            ErrorLevel errorLevel, String desc) {
        // create ExceptionLog object and send to Cynomys Server
        ExceptionLog log = ExceptionLogCreator.create(sysCode, bizCode, bizDesc, errCode, errDesc, sysErrCode,
                sysErrDesc, errorLevel, desc);
        try {
            Message request = new Message()
                    .setHeader(
                            new Header()
                                    .setSysCode(Constant.SYS_CODE)
                                    .setSysName(Constant.SYS_NAME)
                                    .setType(MessageType.REQUEST.value())
                                    .setCode(MessageCode.EXCEPTION_INFO_REQ.value())
                    )
                    .setBody(JSON.toJSONString(log));
            this.cynomysClient.async(request, new ExceptionLogInvokeCallBack(request));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void log(String bizCode, String bizDesc, String errCode, String errDesc, ErrorLevel level, Throwable e) {
        SysErrorInfo sysError = resolver.errorOf(e);
        if (!sysError.getCode().matches("^\\d{3}$")) {
            throw new IllegalArgumentException("SysErrCode必须是3位数字");
        }
        this.log(Constant.SYS_CODE, bizCode, bizDesc, errCode, errDesc, sysError.getCode(), sysError.getDesc(),
                level, infoWithStackTrace(e));
    }

    private String infoWithStackTrace(Throwable e) {
        StringBuilder info = new StringBuilder(e.getClass().getCanonicalName());
        info.append(':').append(e.getMessage());
        for (StackTraceElement stackTraceElement : e.getStackTrace()) {
            info.append('\n').append(stackTraceElement.toString());
        }
        return info.toString();
    }

    @Override
    public void close() throws IOException {
        if (this.cynomysClient != null) {
            this.cynomysClient.shutdown();
        }
    }
}

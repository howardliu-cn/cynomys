package cn.howardliu.cynomys.warn;

import cn.howardliu.cynomys.warn.log.exception.*;
import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.client.common.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.common.Constant;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import cn.howardliu.monitor.cynomys.common.ServiceThread;
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
import java.sql.SQLException;
import java.util.List;

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
    private final ExceptionLogCleanerExecutor cleanerExecutor = new ExceptionLogCleanerExecutor();

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
        this.cleanerExecutor.start();
    }

    public void log(String bizCode, String bizDesc, String errCode, String errDesc, ErrorLevel level, Throwable e) {
        SysErrorInfo sysError = resolver.errorOf(e);
        if (!sysError.getCode().matches("^\\d{3}$")) {
            throw new IllegalArgumentException("SysErrCode必须是3位数字");
        }
        this.log(Constant.SYS_CODE, bizCode, bizDesc, errCode, errDesc, sysError.getCode(), sysError.getDesc(),
                level, infoWithStackTrace(e));
    }

    public void log(String sysCode,
            String bizCode, String bizDesc,
            String errCode, String errDesc,
            String sysErrCode, String sysErrDesc,
            ErrorLevel errorLevel, String desc) {
        // create ExceptionLog object and send to Cynomys Server
        this.log(
                ExceptionLogCreator.create(sysCode, bizCode, bizDesc, errCode, errDesc, sysErrCode,
                        sysErrDesc, errorLevel, desc)
        );
    }

    public void log(ExceptionLog log) {
        String logMsg = JSON.toJSONString(log);
        try {
            Message request = new Message()
                    .setHeader(
                            new Header()
                                    .setSysCode(Constant.SYS_CODE)
                                    .setSysName(Constant.SYS_NAME)
                                    .setType(MessageType.REQUEST.value())
                                    .setCode(MessageCode.EXCEPTION_INFO_REQ.value())
                    )
                    .setBody(logMsg);
            this.cynomysClient.async(request, new ExceptionLogInvokeCallBack(request));
        } catch (Exception e) {
            logger.error("send data to Cynomys Server error", e);
            try {
                ExceptionLogCaching.INSTANCE.upsert(log.getErrId(), logMsg);
            } catch (SQLException e1) {
                logger.error("stage ExceptionLog into DB error", e1);
            }
        }
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
        if (this.cleanerExecutor != null) {
            this.cleanerExecutor.shutdown();
        }
    }

    class ExceptionLogCleanerExecutor extends ServiceThread {
        @Override
        public void run() {
            while (!this.isStopped()) {
                try {
                    List<ExceptionLog> list = ExceptionLogCaching.INSTANCE.list(0, 10);
                    for (ExceptionLog msg : list) {
                        try {
                            WarnLoggingClient.INSTANCE.log(msg);
                            ExceptionLogCaching.INSTANCE.delete(msg.getErrId());
                        } catch (Exception e) {
                            logger.error("resend Exception Log to Cynomys Server exception", e);
                        }
                    }
                } catch (SQLException e) {
                    logger.error("list exception log exception", e);
                }
            }
        }

        @Override
        public String getServiceName() {
            return null;
        }
    }
}

package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.common.LaunchLatch;
import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import cn.howardliu.monitor.cynomys.net.exception.NetConnectException;
import cn.howardliu.monitor.cynomys.net.exception.NetSendRequestException;
import cn.howardliu.monitor.cynomys.net.exception.NetTimeoutException;
import cn.howardliu.monitor.cynomys.net.exception.NetTooMuchRequestException;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageCode;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;

import static cn.howardliu.monitor.cynomys.common.Constant.*;

/**
 * 服务器心跳组件
 *
 * @Class Name HeartLive
 * @Author Jack
 * @Create In 2015年8月25日
 */
public class MonitorChecker implements Health, Closeable {
    private static final Logger logger = LoggerFactory.getLogger(MonitorChecker.class);
    private final CynomysClient cynomysClient;
    private String appName;
    private Long sessionId;
    private byte[] sessionPassword;
    private String appServerPath;
    private Thread m;
    private volatile boolean isMonitorStop = false;
    private AppMonitor appMonitor;

    public MonitorChecker() {
        this.appName = SYS_NAME;
        appMonitor = AppMonitor.instance(SERVER_PORT);
        cynomysClient = CynomysClientManager.INSTANCE
                .getAndCreateCynomysClient(
                        new ClientConfig(),
                        new SimpleChannelEventListener() {
                            @Override
                            public void onChannelClose(String address, Channel channel) {
                                if (cynomysClient.isChannelWriteable()) {
                                    return;
                                }
                                super.onChannelClose(address, channel);
                                reconnection();
                            }

                            @Override
                            public void onChannelException(String address, Channel channel, Throwable cause) {
                                if (cynomysClient.isChannelWriteable()) {
                                    return;
                                }
                                super.onChannelException(address, channel, cause);
                                reconnection();
                            }

                            private void reconnection() {
                                try {
                                    TimeUnit.MILLISECONDS
                                            .sleep(cynomysClient.getNettyClientConfig().getRelinkDelayMillis());
                                    cynomysClient.connect();
                                } catch (Exception e) {
                                    logger.error("reconnect exception", e);
                                }
                            }
                        }
                );
        LaunchLatch.CLIENT_INIT.start();
        cynomysClient.updateAddressList(SERVER_LIST);
        cynomysClient.start();
        try {
            this.cynomysClient.connect();
        } catch (InterruptedException e) {
            logger.error("got an exception when connecting to Cynomys Server", e);
        }
    }

    /**
     * @Return the Thread m
     */
    public Thread getM() {
        return m;
    }

    /**
     * @Return the AppMonitor appMonitor
     */
    public AppMonitor getAppMonitor() {
        return appMonitor;
    }

    /**
     * @Param AppMonitor appMonitor to set
     */
    public void setAppMonitor(AppMonitor appMonitor) {
        this.appMonitor = appMonitor;
    }

    /**
     * @Return the Long sessionId
     */
    public Long getSessionId() {
        return sessionId;
    }

    /**
     * @Param Long sessionId to set
     */
    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @Return the byte[] sessionPassword
     */
    public byte[] getSessionPassword() {
        return sessionPassword;
    }

    /**
     * @Param byte[] sessionPassword to set
     */
    public void setSessionPassword(byte[] sessionPassword) {
        this.sessionPassword = sessionPassword;
    }

    /**
     * @Return the Boolean isMonitorStopBoolean
     */
    public Boolean getIsMonitorStopBoolean() {
        return isMonitorStop;
    }

    /**
     * @Param Boolean isMonitorStopBoolean to set
     */
    public void setIsMonitorStopBoolean(Boolean isMonitorStopBoolean) {
        this.isMonitorStop = isMonitorStopBoolean;
    }

    /**
     * @Return the String appServerPath
     */
    public String getAppServerPath() {
        return appServerPath;
    }

    /**
     * @Param String appServerPath to set
     */
    public void setAppServerPath(String appServerPath) {
        this.appServerPath = appServerPath;
    }

    /**
     * @Return the String appName
     */
    public String getAppName() {
        return appName;
    }

    /**
     * @Param String appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * 实例JVM及系统监控状况服务
     *
     * @param splitTime 毫秒millis
     * @Methods Name hearthCheck
     * @Create In 2015年8月26日 By Jack
     */
    private void hearthCheck(final Long splitTime) {
        isMonitorStop = false;
        this.m = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!isMonitorStop) {
                        // 1.构建系统实时状态并存储
                        String appInfo = appMonitor.buildAppInfo();
                        if (appInfo != null) {
                            sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(SYS_NAME)
                                                            .setSysCode(SYS_CODE)
                                                            .setLength(appInfo.length())
                                                            .setType(MessageType.REQUEST.value())
                                                            .setCode(MessageCode.APP_INFO_REQ.value())
                                            )
                                            .setBody(appInfo)
                            );
                        }

                        // 2.SQL计数信息
                        String sqlInfo = appMonitor.buildSQLCountsInfo();
                        if (sqlInfo != null) {
                            sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(SYS_NAME)
                                                            .setSysCode(SYS_CODE)
                                                            .setLength(sqlInfo.length())
                                                            .setType(MessageType.REQUEST.value())
                                                            .setCode(MessageCode.SQL_INFO_REQ.value())
                                            )
                                            .setBody(sqlInfo)
                            );
                        }

                        // 3. 请求计数信息
                        String requestInfo = appMonitor.buildRequestCountInfo();
                        if (requestInfo != null) {
                            sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(SYS_NAME)
                                                            .setSysCode(SYS_CODE)
                                                            .setLength(requestInfo.length())
                                                            .setType(MessageType.REQUEST.value())
                                                            .setCode(MessageCode.REQUEST_INFO_REQ.value())
                                            )
                                            .setBody(requestInfo)
                            );
                        }

                        // 4.进入休眠，等待下一次执行，默认5分钟执行一次
                        Thread.sleep(splitTime);
                    }
                    logger.debug("Health Monitor Service is Stop!");
                } catch (InterruptedException e) {
                    logger.error("cannot load monitor module", e);
                    Thread.currentThread().interrupt();
                }
            }
        }, "monitor-agent-hearth-thread");
        m.setDaemon(true);
        m.start();
    }

    public void startHealth(String status) {
        // TODO read from config file
        hearthCheck(60000L);
    }

    public void updateHealth(String status) {
        try {
            // 更新自身节点状态
            Object[] tagArgs = {SYS_NAME, SYS_CODE, status};
            String rootDesc = SYS_DESC;
            rootDesc = MessageFormat.format(rootDesc, tagArgs);
            System.err.println(rootDesc);
            // TODO check this function
        } catch (Exception e) {
            logger.error("cannot update monitor info", e);
        }
    }

    public void shutdownHealth(String status) {
        // 1.停止实例信息获取模块
        this.isMonitorStop = this.m != null;
        // 2.停止zk 链接监控线程
        // TODO stop
    }

    @Override
    public void restartHealth(String status) {
        shutdownHealth(status);
        try {
            this.cynomysClient.connect();
        } catch (InterruptedException e) {
            logger.error("cynomys client reconnect exception", e);
        }
    }

    @Override
    public void setListensePort(Integer port) {
        this.appMonitor.setPort(port);
    }

    private void sendData(Message request) {
        try {
            this.cynomysClient.async(request, null);
        } catch (InterruptedException | NetTimeoutException | NetSendRequestException | NetTooMuchRequestException | NetConnectException e) {
            logger.error("send monitor data to SERVER exception", e);
        }
    }

    @Override
    public void close() throws IOException {
        this.isMonitorStop = true;
        this.cynomysClient.shutdown();
    }
}

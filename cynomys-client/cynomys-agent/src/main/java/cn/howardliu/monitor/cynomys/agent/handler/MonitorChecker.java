package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.common.CommonParameters;
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
        this.appName = CommonParameters.getSysName();
        appMonitor = AppMonitor.instance();
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
        cynomysClient.updateAddressList(CommonParameters.getServerList());
        cynomysClient.start();
        try {
            this.cynomysClient.connect();
        } catch (InterruptedException e) {
            logger.error("got an exception when connecting to Cynomys Server", e);
            Thread.currentThread().interrupt();
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
        this.m = new Thread(() -> {
            try {
                while (!isMonitorStop) {
                    // 1.构建系统实时状态并存储
                    sendInfo(appMonitor.buildAppInfo(), MessageCode.APP_INFO_REQ);
                    // 2.SQL计数信息
                    sendInfo(appMonitor.buildSQLCountsInfo(), MessageCode.SQL_INFO_REQ);
                    // 3. 请求计数信息
                    sendInfo(appMonitor.buildRequestCountInfo(), MessageCode.REQUEST_INFO_REQ);
                    // 4. 隔天清理数据
                    // 5.进入休眠，等待下一次执行，默认5分钟执行一次
                    Thread.sleep(splitTime);
                }
                logger.debug("Health Monitor Service is Stop!");
            } catch (InterruptedException e) {
                logger.error("cannot load monitor module", e);
                Thread.currentThread().interrupt();
            }
        }, "monitor-agent-hearth-thread");
        m.setDaemon(true);
        m.start();
    }

    private void sendInfo(final String info, final MessageCode sqlInfoReq) {
        if (info != null) {
            sendData(
                    new Message()
                            .setHeader(
                                    new Header()
                                            .setSysName(CommonParameters.getSysName())
                                            .setSysCode(CommonParameters.getSysCode())
                                            .setLength(info.length())
                                            .setType(MessageType.REQUEST.value())
                                            .setCode(sqlInfoReq.value())
                            )
                            .setBody(info)
            );
        }
    }

    private void cleanPreInfo() {

    }

    public void startHealth(String status) {
        // TODO read from config file
        hearthCheck(60000L);
    }

    public void updateHealth(String status) {
        try {
            // 更新自身节点状态
            Object[] tagArgs = {CommonParameters.getSysName(), CommonParameters.getSysCode(), status};
            String rootDesc = CommonParameters.getSysDesc();
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
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void setListensePort(Integer port) {
        CommonParameters.setServerPort(port);
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

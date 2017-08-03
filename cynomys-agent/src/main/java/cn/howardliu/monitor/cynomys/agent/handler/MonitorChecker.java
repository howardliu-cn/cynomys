/**
 * @Probject Name: netty-wfj-base-dev
 * @Path: com.wfj.netty.monitorHeartLive.java
 * @Create By Jack
 * @Create In 2015年8月25日 上午9:49:49
 */
package cn.howardliu.monitor.cynomys.agent.handler;

import cn.howardliu.monitor.cynomys.agent.common.Constant;
import cn.howardliu.monitor.cynomys.agent.conf.EnvPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.conf.PropertyAdapter;
import cn.howardliu.monitor.cynomys.agent.conf.SystemPropertyConfig;
import cn.howardliu.monitor.cynomys.agent.net.ServerInfo;
import cn.howardliu.monitor.cynomys.agent.net.operator.ConfigInfoOperator;
import cn.howardliu.monitor.cynomys.agent.net.operator.SocketMonitorDataOperator;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务器心跳组件
 *
 * @Class Name HeartLive
 * @Author Jack
 * @Create In 2015年8月25日
 */
public class MonitorChecker implements Health {
    private static final Logger logger = LoggerFactory.getLogger(MonitorChecker.class);
    private String appName;
    private Long sessionId;
    private byte[] sessionPassword;
    private String appServerPath;
    private Thread m;
    private volatile boolean isMonitorStop = false;
    private AppMonitor appMonitor;
    private ConfigInfoOperator configInfoOperator;
    private SocketMonitorDataOperator operator;

    public MonitorChecker(Integer p, ServletContext sc, ConfigInfoOperator configInfoOperator) {
        this.appName = SystemPropertyConfig.getContextProperty("system.setting.context-name");
        appMonitor = AppMonitor.instance(p, sc);
        this.configInfoOperator = configInfoOperator;
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
            public void run() {
                try {
                    while (!isMonitorStop) {
                        // 1.构建系统实时状态并存储
                        String appInfo = appMonitor.buildAppInfo();
                        if (appInfo != null) {
                            operator.sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(cn.howardliu.monitor.cynomys.common.Constant.SYS_NAME)
                                                            .setSysCode(cn.howardliu.monitor.cynomys.common.Constant.SYS_CODE)
                                                            .setLength(appInfo.length())
                                                            .setType(MessageType.APP_INFO_REQ.value())
                                            )
                                            .setBody(appInfo)
                            );
                        }

                        // 2.SQL计数信息
                        String sqlInfo = appMonitor.buildSQLCountsInfo();
                        if (sqlInfo != null) {
                            operator.sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(cn.howardliu.monitor.cynomys.common.Constant.SYS_NAME)
                                                            .setSysCode(cn.howardliu.monitor.cynomys.common.Constant.SYS_CODE)
                                                            .setLength(sqlInfo.length())
                                                            .setType(MessageType.SQL_INFO_REQ.value())
                                            )
                                            .setBody(sqlInfo)
                            );
                        }

                        // 3. 请求计数信息
                        String requestInfo = appMonitor.buildRequestCountInfo();
                        if (requestInfo != null) {
                            operator.sendData(
                                    new Message()
                                            .setHeader(
                                                    new Header()
                                                            .setSysName(cn.howardliu.monitor.cynomys.common.Constant.SYS_NAME)
                                                            .setSysCode(cn.howardliu.monitor.cynomys.common.Constant.SYS_CODE)
                                                            .setLength(requestInfo.length())
                                                            .setType(MessageType.REQUEST_INFO_REQ.value())
                                            )
                                            .setBody(requestInfo)
                            );
                        }

                        // 4.进入休眠，等待下一次执行，默认5分钟执行一次
                        Thread.sleep(splitTime);
                    }
                    logger.debug("Health Monitor Service is Stop!");
                } catch (InterruptedException e) {
                    logger.error(EnvPropertyConfig.getContextProperty("env.setting.server.error.00001012"));
                    logger.error("Details: " + e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        }, "monitor-agent-hearth-thread");
        m.start();
    }

    public void startHealth(String status) {

//        try {
//            // 1.初始化跟踪节点
//            this.appServerPath = appMonitor.buildMonitorRootInfo(status);
        // 2 启动实例状态监控服务
        Long stepTime = Long
                .valueOf(EnvPropertyConfig.getContextProperty("env.setting.server.monitor.checker.sleeptime"));
        link();
        hearthCheck(stepTime);
//        } catch (InterruptedException e) {
//            logger.error(EnvPropertyConfig.getContextProperty("env.setting.server.error.00001010"));
//            logger.error("Details: " + e.getMessage());
//        }

    }

    private List<ServerInfo> getServerInfoList() {
        try {
            List<ServerInfo> proxyServers = new ArrayList<>(
                    this.configInfoOperator
                            .reset()
                            .start()
                            .getProxyServers()
            );
            Collections.sort(proxyServers, new Comparator<ServerInfo>() {
                @Override
                public int compare(ServerInfo o1, ServerInfo o2) {
                    return -(o1.getConnectCount() - o2.getConnectCount());
                }
            });
            return proxyServers;
        } catch (Exception e) {
            try {
                TimeUnit.SECONDS.sleep(5);
            } catch (InterruptedException ignored) {
            }
            return getServerInfoList();
        }
    }

    public synchronized void link() {
        List<ServerInfo> serverInfoList = getServerInfoList();
        System.err.println("获取连接列表：" + serverInfoList);
        ServerInfo serverInfo = serverInfoList.get(0);
        if (operator == null) {
            operator = new SocketMonitorDataOperator(serverInfo.getIp(), serverInfo.getPort(), this);
        } else {
            operator.reconnect(serverInfo.getIp(), serverInfo.getPort());
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    operator.start();
                    if (!operator.connect()) {
                        link();
                    }
                } catch (Exception e) {
                    logger.error("disconnect to server", e);
                }
            }
        });
        thread.setDaemon(true);
        thread.setName("SocketMonitorDataOperator-start-thread");
        thread.start();
    }

    public void updateHealth(String status) {
        try {
            // 更新自身节点状态
            Object[] tagArgs = {status};
            String rootDesc = SystemPropertyConfig.getContextProperty("system.setting.context-desc");
            rootDesc = PropertyAdapter.formatter(rootDesc, tagArgs);
            System.err.println(rootDesc);
        } catch (Exception e) {
            logger.error(EnvPropertyConfig.getContextProperty("env.setting.server.error.00001011"));
            logger.error("Details: " + e.getMessage());
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
        // TODO reconnect
        try {
            this.operator.connect();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void setListensePort(Integer port) {
        this.appMonitor.setPort(port);
    }

}

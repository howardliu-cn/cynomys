package cn.howardliu.monitor.cynomys.proxy.server;

import cn.howardliu.gear.kafka.KafkaProducerWrapper;
import cn.howardliu.gear.zk.ZkClientFactoryBuilder;
import cn.howardliu.gear.zk.ZkConfig;
import cn.howardliu.monitor.cynomys.net.NetServer;
import cn.howardliu.monitor.cynomys.net.netty.NettyNetServer;
import cn.howardliu.monitor.cynomys.net.netty.NettyServerConfig;
import cn.howardliu.monitor.cynomys.proxy.ServerContext;
import cn.howardliu.monitor.cynomys.proxy.config.ProxyConfig;
import cn.howardliu.monitor.cynomys.proxy.config.ServerConfig;
import cn.howardliu.monitor.cynomys.proxy.config.SystemSetting;
import cn.howardliu.monitor.cynomys.proxy.listener.LinkEventListener;
import cn.howardliu.monitor.cynomys.proxy.processor.*;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.utils.CloseableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static cn.howardliu.monitor.cynomys.net.struct.MessageCode.*;
import static org.apache.kafka.clients.producer.ProducerConfig.*;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProxyServer extends AbstractServer {
    private static final Logger logger = LoggerFactory.getLogger(ProxyServer.class);
    private final ServerConfig serverConfig;

    private final KafkaProducerWrapper<String, String> kafkaProducerWrapper;
    private final CuratorFramework zkClient;
    private final NetServer netServer;

    private ServerContext context;

    private ExecutorService heartbeatExecutor;
    private ExecutorService appInfoActionExecutor;
    private ExecutorService requestInfoActionExecutor;
    private ExecutorService sqlInfoActionExecutor;
    private ExecutorService exceptionInfoActionExecutor;

    public ProxyServer(ServerConfig serverConfig) {
        super(serverConfig.getListenPort(), serverConfig.getCtrlPort());

        this.serverConfig = serverConfig;

        Properties config = new Properties();
        config.put(BOOTSTRAP_SERVERS_CONFIG, SystemSetting.SYSTEM_SETTING.getKafkaBootstrapServers());
        config.put(ACKS_CONFIG, SystemSetting.SYSTEM_SETTING.getKafkaAcks());
        config.put(RETRIES_CONFIG, SystemSetting.SYSTEM_SETTING.getKafkaRetries());
        config.put(BATCH_SIZE_CONFIG, SystemSetting.SYSTEM_SETTING.getKafkaBatchSize());
        config.put(MAX_REQUEST_SIZE_CONFIG, SystemSetting.SYSTEM_SETTING.getKafkaMaxRequestSize());
        this.kafkaProducerWrapper = new KafkaProducerWrapper<>(config);

        this.zkClient = new ZkClientFactoryBuilder()
                .zkAddresses(SystemSetting.SYSTEM_SETTING.getZkAddresses())
                .namespace(SystemSetting.SYSTEM_SETTING.getZkNamespace())
                .config(ZkConfig.JuteMaxBuffer.key, 10 * 1024 * 1024)
                .build()
                .createClient();

        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(port);
        nettyServerConfig.setServerSocketMaxFrameLength(ProxyConfig.PROXY_CONFIG.getMaxFrameLength());
        nettyServerConfig.setServerChannelMaxIdleTimeSeconds(ProxyConfig.PROXY_CONFIG.getTimeoutSeconds());
        this.netServer = new NettyNetServer(nettyServerConfig, new LinkEventListener(zkClient));
    }

    public void initialize() {
        heartbeatExecutor = Executors.newFixedThreadPool(
                this.serverConfig.getHeartbeatActionThreadPoolNums(),
                new DefaultThreadFactory("heartbeat-action-thread")
        );
        appInfoActionExecutor = Executors.newFixedThreadPool(
                this.serverConfig.getAppInfoActionThreadPoolNums(),
                new DefaultThreadFactory("app-info-action-thread")
        );
        requestInfoActionExecutor = Executors.newFixedThreadPool(
                this.serverConfig.getRequestInfoActionThreadPoolNums(),
                new DefaultThreadFactory("request-info-action-thread")
        );
        sqlInfoActionExecutor = Executors.newFixedThreadPool(
                this.serverConfig.getSqlInfoActionThreadPoolNums(),
                new DefaultThreadFactory("sql-info-action-thread")
        );
        exceptionInfoActionExecutor = Executors.newFixedThreadPool(
                this.serverConfig.getExceptionInfoActionThreadPoolNums(),
                new DefaultThreadFactory("exception-info-action-thread")
        );
    }

    public void registProcessor() {
        this.netServer.registProcessor(HEARTBEAT_REQ.value(), new HeartbeatProcessor(), heartbeatExecutor);
        this.netServer.registProcessor(APP_INFO_REQ.value(), new AppInfo2ZkProcessor(zkClient), appInfoActionExecutor);
        this.netServer.registProcessor(REQUEST_INFO_REQ.value(), new RequestInfo2KafkaProcessor(kafkaProducerWrapper), requestInfoActionExecutor);
        this.netServer.registProcessor(SQL_INFO_REQ.value(), new SqlInfo2KafkaProcessor(kafkaProducerWrapper), sqlInfoActionExecutor);
        this.netServer.registProcessor(EXCEPTION_INFO_REQ.value(), new ExceptionInfo2KafkaProcessor(kafkaProducerWrapper), exceptionInfoActionExecutor);
    }

    @Override
    public void startup() {
        logger.info("begin to starting, use server port [{}], control port [{}]", port, cport);
        this.context = ServerContext.getInstance(this);
        try {
            ctrl();
            if (!this.netServer.isStarted()) {
                this.netServer.start();
            }
        } catch (Exception e) {
            logger.error("an exception thrown when server starting up", e);
        }
    }

    @Override
    public void shutdown() {
        this.netServer.shutdown();
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        if (appInfoActionExecutor != null) {
            appInfoActionExecutor.shutdown();
        }
        if (requestInfoActionExecutor != null) {
            requestInfoActionExecutor.shutdown();
        }
        if (sqlInfoActionExecutor != null) {
            sqlInfoActionExecutor.shutdown();
        }
        if (exceptionInfoActionExecutor != null) {
            exceptionInfoActionExecutor.shutdown();
        }
        CloseableUtils.closeQuietly(this.zkClient);
        CloseableUtils.closeQuietly(this.kafkaProducerWrapper);
    }

    public ServerContext getContext() {
        return context;
    }
}

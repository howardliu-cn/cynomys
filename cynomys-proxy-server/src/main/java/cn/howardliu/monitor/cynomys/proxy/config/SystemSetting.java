package cn.howardliu.monitor.cynomys.proxy.config;

import cn.howardliu.gear.commons.annotation.Key;
import cn.howardliu.gear.commons.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <br>created at 17-7-29
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public enum SystemSetting {
    SYSTEM_SETTING;

    private static final Logger logger = LoggerFactory.getLogger(SystemSetting.class);

    static {
        try (InputStream in = SystemSetting.class.getResourceAsStream("/conf/system-setting.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            properties.putAll(System.getenv());
            properties.putAll(System.getProperties());
            SYSTEM_SETTING.clone(PropertiesUtils.parseObject(properties, SystemSetting.class));
        } catch (IOException e) {
            logger.error("Got an IOException when loading system-setting.properties file", e);
            System.exit(1);
        }
    }

    @Key("system.setting.zk.addresses")
    private String zkAddresses;
    @Key("system.setting.zk.namespace")
    private String zkNamespace;
    @Key("system.setting.kafka.bootstrap.servers")
    private String kafkaBootstrapServers;
    @Key("system.setting.kafka.zookeeper.connect")
    private String kafkaZookeeperConnect;
    @Key("system.setting.kafka.acks")
    private String kafkaAcks = "all";
    @Key("system.setting.kafka.retries")
    private int kafkaRetries = 0;
    @Key("system.setting.kafka.batch.size")
    private int kafkaBatchSize = 16 * 1024;
    @Key("system.setting.kafka.max.request.size")
    private int kafkaMaxRequestSize = 1024 * 1024;
    @Key("system.setting.kafka.topic.app")
    private String kafkaTopicApp;
    @Key("system.setting.kafka.topic.sql")
    private String kafkaTopicSql;
    @Key("system.setting.kafka.topic.request")
    private String kafkaTopicRequest;

    private void clone(SystemSetting source) {
        this.zkAddresses = source.getZkAddresses();
        this.zkNamespace = source.getZkNamespace();

        this.kafkaBootstrapServers = source.getKafkaBootstrapServers();
        this.kafkaZookeeperConnect = source.getKafkaZookeeperConnect();
        this.kafkaAcks = source.getKafkaAcks();
        this.kafkaRetries = source.getKafkaRetries();
        this.kafkaBatchSize = source.getKafkaBatchSize();
        this.kafkaMaxRequestSize = source.getKafkaMaxRequestSize();
        this.kafkaTopicApp = source.getKafkaTopicApp();
        this.kafkaTopicSql = source.getKafkaTopicSql();
        this.kafkaTopicRequest = source.getKafkaTopicRequest();
    }

    public String getZkAddresses() {
        return zkAddresses;
    }

    public String getZkNamespace() {
        return zkNamespace;
    }

    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getKafkaZookeeperConnect() {
        return kafkaZookeeperConnect;
    }

    public String getKafkaAcks() {
        return kafkaAcks;
    }

    public int getKafkaRetries() {
        return kafkaRetries;
    }

    public int getKafkaBatchSize() {
        return kafkaBatchSize;
    }

    public int getKafkaMaxRequestSize() {
        return kafkaMaxRequestSize;
    }

    public String getKafkaTopicApp() {
        return kafkaTopicApp;
    }

    public String getKafkaTopicSql() {
        return kafkaTopicSql;
    }

    public String getKafkaTopicRequest() {
        return kafkaTopicRequest;
    }
}

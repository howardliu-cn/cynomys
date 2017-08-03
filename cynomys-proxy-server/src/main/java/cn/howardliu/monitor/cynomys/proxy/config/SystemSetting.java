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
    private static SystemSettingParam param;

    static {
        try (InputStream in = SystemSetting.class.getResourceAsStream("/conf/system-setting.properties")) {
            Properties properties = new Properties();
            properties.load(in);
            properties.putAll(System.getenv());
            properties.putAll(System.getProperties());
            param = PropertiesUtils.parseObject(properties, SystemSettingParam.class);
        } catch (IOException e) {
            logger.error("Got an IOException when loading system-setting.properties file", e);
            System.exit(1);
        }
    }

    public String getZkAddresses() {
        return param.getZkAddresses();
    }

    public String getZkNamespace() {
        return param.getZkNamespace();
    }

    public boolean isCreatePathIfNeeded() {
        return param.isCreatePathIfNeeded();
    }

    public String getKafkaBootstrapServers() {
        return param.getKafkaBootstrapServers();
    }

    public String getKafkaZookeeperConnect() {
        return param.getKafkaZookeeperConnect();
    }

    public String getKafkaAcks() {
        return param.getKafkaAcks();
    }

    public int getKafkaRetries() {
        return param.getKafkaRetries();
    }

    public int getKafkaBatchSize() {
        return param.getKafkaBatchSize();
    }

    public int getKafkaMaxRequestSize() {
        return param.getKafkaMaxRequestSize();
    }

    public String getKafkaTopicApp() {
        return param.getKafkaTopicApp();
    }

    public String getKafkaTopicSql() {
        return param.getKafkaTopicSql();
    }

    public String getKafkaTopicRequest() {
        return param.getKafkaTopicRequest();
    }

    public static class SystemSettingParam {
        @Key("system.setting.zk.addresses")
        private String zkAddresses;
        @Key("system.setting.zk.namespace")
        private String zkNamespace;
        @Key("system.setting.zk.createPathIfNeeded")
        private boolean createPathIfNeeded = Boolean.TRUE;

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

        public String getZkAddresses() {
            return zkAddresses;
        }

        public void setZkAddresses(String zkAddresses) {
            this.zkAddresses = zkAddresses;
        }

        public String getZkNamespace() {
            return zkNamespace;
        }

        public void setZkNamespace(String zkNamespace) {
            this.zkNamespace = zkNamespace;
        }

        public boolean isCreatePathIfNeeded() {
            return createPathIfNeeded;
        }

        public void setCreatePathIfNeeded(boolean createPathIfNeeded) {
            this.createPathIfNeeded = createPathIfNeeded;
        }

        public String getKafkaBootstrapServers() {
            return kafkaBootstrapServers;
        }

        public void setKafkaBootstrapServers(String kafkaBootstrapServers) {
            this.kafkaBootstrapServers = kafkaBootstrapServers;
        }

        public String getKafkaZookeeperConnect() {
            return kafkaZookeeperConnect;
        }

        public void setKafkaZookeeperConnect(String kafkaZookeeperConnect) {
            this.kafkaZookeeperConnect = kafkaZookeeperConnect;
        }

        public String getKafkaAcks() {
            return kafkaAcks;
        }

        public void setKafkaAcks(String kafkaAcks) {
            this.kafkaAcks = kafkaAcks;
        }

        public int getKafkaRetries() {
            return kafkaRetries;
        }

        public void setKafkaRetries(int kafkaRetries) {
            this.kafkaRetries = kafkaRetries;
        }

        public int getKafkaBatchSize() {
            return kafkaBatchSize;
        }

        public void setKafkaBatchSize(int kafkaBatchSize) {
            this.kafkaBatchSize = kafkaBatchSize;
        }

        public int getKafkaMaxRequestSize() {
            return kafkaMaxRequestSize;
        }

        public void setKafkaMaxRequestSize(int kafkaMaxRequestSize) {
            this.kafkaMaxRequestSize = kafkaMaxRequestSize;
        }

        public String getKafkaTopicApp() {
            return kafkaTopicApp;
        }

        public void setKafkaTopicApp(String kafkaTopicApp) {
            this.kafkaTopicApp = kafkaTopicApp;
        }

        public String getKafkaTopicSql() {
            return kafkaTopicSql;
        }

        public void setKafkaTopicSql(String kafkaTopicSql) {
            this.kafkaTopicSql = kafkaTopicSql;
        }

        public String getKafkaTopicRequest() {
            return kafkaTopicRequest;
        }

        public void setKafkaTopicRequest(String kafkaTopicRequest) {
            this.kafkaTopicRequest = kafkaTopicRequest;
        }
    }
}

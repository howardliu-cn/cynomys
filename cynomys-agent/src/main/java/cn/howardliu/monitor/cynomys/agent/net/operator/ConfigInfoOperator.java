package cn.howardliu.monitor.cynomys.agent.net.operator;

import cn.howardliu.monitor.cynomys.agent.net.ServerInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static cn.howardliu.monitor.cynomys.agent.net.ServerInfo.ServerType.LAN;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ConfigInfoOperator {
    private static final Logger logger = LoggerFactory.getLogger(ConfigInfoOperator.class);
    private static final Map<String, ConfigInfoOperator> operatorSet = Collections
            .synchronizedMap(new HashMap<String, ConfigInfoOperator>());
    private static final Object readServerListLock = new Object();
    private static final ReadWriteLock lock = new ReentrantReadWriteLock();

    private String ip;
    private int port;
    private Map<String, ServerInfo> proxyServers = Collections.synchronizedMap(new HashMap<String, ServerInfo>());
    private volatile boolean isRead = false;

    private ConfigInfoOperator(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public synchronized static ConfigInfoOperator instance(String ip, int port) {
        assert ip != null;
        assert port > 0;
        lock.readLock().lock();
        try {
            ConfigInfoOperator operator = operatorSet.get(ip + port);
            if (operator != null) {
                return operator;
            }
        } finally {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try {
            ConfigInfoOperator operator = new ConfigInfoOperator(ip, port);
            operatorSet.put(ip + port, operator);
            return operator;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public ConfigInfoOperator reset() {
        isRead = false;
        proxyServers.clear();
        return this;
    }

    public ConfigInfoOperator start() throws Exception {
        readConfInfo();
        return this;
    }

    private void readConfInfo() {
        try {
            Response response = new OkHttpClient.Builder()
                    .connectTimeout(30L, TimeUnit.SECONDS)
                    .readTimeout(30L, TimeUnit.SECONDS)
                    .writeTimeout(30L, TimeUnit.SECONDS)
                    .build()
                    .newCall(
                            new Request.Builder()
                                    .url("http://192.168.7.21:8080/config-server/config/proxyserver/list.htm")
                                    .build()
                    )
                    .execute();

            String msg = response.body().string();
//            JSONArray array = JSON.parseArray(msg);
//            if (logger.isDebugEnabled()) {
//                logger.debug("receive message: {}", msg);
//            }
//            if (array.size() == 0) {
//                TimeUnit.MILLISECONDS.sleep(100);
//                readConfInfo();
//            }
//            for (int i = 0; i < array.size(); i++) {
//                JSONObject json = array.getJSONObj  ect(i);
                this.addServerInfo(
                        new ServerInfo(
//                                json.getString("ip"),
//                                json.getInteger("port"),
//                                json.getInteger("connectCount"),
//                                LAN
                                "192.168.7.21",
                                1666,
                                0,
                                LAN
                        )
                );
//            }
            this.read();
        } catch (Exception e) {
            e.printStackTrace();
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException ignored) {
            }
            readConfInfo();
        }
    }

    public void read() {
        this.isRead = true;
        synchronized (readServerListLock) {
            try {
                readServerListLock.notify();
            } catch (Exception e) {
                // TODO do action
                logger.error("读取解锁异常", e);
            }
        }
    }

    public synchronized ConfigInfoOperator addServerInfo(ServerInfo server) {
        assert server != null;
        if (proxyServers.containsValue(server)) {
            proxyServers.get(server.getTag()).setConnectCount(server.getConnectCount());
        } else {
            proxyServers.put(server.getTag(), server);
        }
        return this;
    }

    public Collection<ServerInfo> getProxyServers() {
        synchronized (readServerListLock) {
            while (!isRead) {
                try {
                    readServerListLock.wait();
                } catch (InterruptedException ignored) {
                }
            }
            return proxyServers.values();
        }
    }
}

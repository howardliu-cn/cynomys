package cn.howardliu.monitor.cynomys.client;

import cn.howardliu.monitor.cynomys.common.CynomysVersion;
import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.howardliu.monitor.cynomys.common.Constant.VERSION_KEY;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public enum CynomysClientManager {
    INSTANCE;

    private static final Logger logger = LoggerFactory.getLogger(CynomysClientManager.class);

    static {
        System.setProperty(VERSION_KEY, Integer.toString(CynomysVersion.CURRENT_VERSION));
    }

    private AtomicInteger factoryIndexGenerator = new AtomicInteger();
    private ConcurrentMap<String, CynomysClient> cynomysClientTable = new ConcurrentHashMap<>();

    public CynomysClient getAndCreateCynomysClient(final ClientConfig clientConfig) {
        String clientId = clientConfig.buildClientId();
        CynomysClient cynomysClient = cynomysClientTable.get(clientId);
        if (cynomysClient == null) {
            cynomysClient = this.cynomysClientTable.putIfAbsent(
                    clientId,
                    new CynomysClient(new NettyClientConfig(clientId))
            );
        }
        return cynomysClient;
    }

    public void removeCynomysClient(final String clientId) {
        this.cynomysClientTable.remove(clientId);
    }
}

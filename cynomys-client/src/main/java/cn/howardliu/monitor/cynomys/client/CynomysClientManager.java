package cn.howardliu.monitor.cynomys.client;

import cn.howardliu.monitor.cynomys.common.CynomysVersion;
import cn.howardliu.monitor.cynomys.net.ChannelEventListener;
import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

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

    private ConcurrentMap<String, CynomysClient> cynomysClientTable = new ConcurrentHashMap<>();

    public CynomysClient getAndCreateCynomysClient(final ClientConfig clientConfig) {
        return this.getAndCreateCynomysClient(clientConfig, null);
    }

    public CynomysClient getAndCreateCynomysClient(final ClientConfig clientConfig,
            final ChannelEventListener channelEventListener) {
        String clientId = clientConfig.buildClientId();
        CynomysClient cynomysClient = cynomysClientTable.get(clientId);
        if (cynomysClient == null) {
            cynomysClient = new CynomysClient(new NettyClientConfig(clientId), channelEventListener);
            CynomysClient prev = this.cynomysClientTable.putIfAbsent(clientId, cynomysClient);
            if (prev != null) {
                cynomysClient = prev;
                logger.warn("Returned Previous CynomysClient for clientId:[{}]", clientId);
            } else {
                logger.info("Created new CynomysClient for clientId:[{}]", clientId);
            }
        }
        return cynomysClient;
    }

    public void removeCynomysClient(final String clientId) {
        this.cynomysClientTable.remove(clientId);
    }
}

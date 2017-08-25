package cn.howardliu.monitor.cynomys.client;

import cn.howardliu.monitor.cynomys.client.common.ClientConfig;
import cn.howardliu.monitor.cynomys.client.common.CynomysClient;
import cn.howardliu.monitor.cynomys.client.common.CynomysClientManager;
import cn.howardliu.monitor.cynomys.net.SimpleChannelEventListener;
import io.netty.channel.Channel;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @since 0.0.1
 */
public class CynomysClientManagerTest {
    private CynomysClient cynomysClient;

    @Test
    public void getAndCreateCynomysClient() throws Exception {
        CynomysClient cynomysClient = CynomysClientManager.INSTANCE.getAndCreateCynomysClient(new ClientConfig());
        Assert.assertNotNull(cynomysClient);
    }

    @Test
    public void test() throws Exception {
        cynomysClient = CynomysClientManager.INSTANCE
                .getAndCreateCynomysClient(
                        new ClientConfig(),
                        new SimpleChannelEventListener() {
                            @Override
                            public void onChannelClose(String address, Channel channel) {
                                super.onChannelClose(address, channel);
                                reconnection();
                            }

                            @Override
                            public void onChannelException(String address, Channel channel, Throwable cause) {
                                super.onChannelException(address, channel, cause);
                                reconnection();
                            }

                            private void reconnection() {
                                try {
                                    TimeUnit.MILLISECONDS
                                            .sleep(cynomysClient.getNettyClientConfig().getRelinkDelayMillis());
                                    cynomysClient.connect();
                                } catch (InterruptedException ignored) {
                                }
                            }
                        }
                );
        cynomysClient.updateAddressList("127.0.0.1:7911");
        cynomysClient.start();
        cynomysClient.connect();

        TimeUnit.MINUTES.sleep(10);
    }
}
package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.netty.NettyClientConfig;
import cn.howardliu.monitor.cynomys.net.netty.NettyNetClient;
import cn.howardliu.monitor.cynomys.net.struct.Header;
import cn.howardliu.monitor.cynomys.net.struct.Message;
import cn.howardliu.monitor.cynomys.net.struct.MessageType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class NettyNetClientTest {
    private static NetClient netClient;

    @Before
    public void setUp() throws Exception {
        netClient = new NettyNetClient(new NettyClientConfig());
        netClient.updateAddressList(Arrays.asList("127.0.0.1:7911", "localhost:7912"));
        netClient.start();
    }

    @After
    public void tearDown() throws Exception {
        netClient.shutdown();
    }

    @Test
    public void async() throws Exception {
        netClient.async(
                new Message()
                        .setHeader(
                                new Header()
                                        .setSysCode("001")
                                        .setSysName("test-client")
                                        .setType(MessageType.HEARTBEAT_REQ.value())
                        )
        );

        TimeUnit.SECONDS.sleep(60);
    }
}
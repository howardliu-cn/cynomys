package cn.howardliu.monitor.cynomys.proxy.zk;

import cn.howardliu.gear.zk.ZkClientFactoryBuilder;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * <br>created at 17-8-17
 *
 * @author liuxh
 * @since 0.0.1
 */
@Ignore
public class ZkClientTest {
    private CuratorFramework client;
    private CuratorFramework client2;
    private PathChildrenCache cache;
    private PathChildrenCache cache2;
    private PathChildrenCacheListener pathChildrenCacheListener = (client, event) -> {
        String path = event.getData().getPath();
        switch (event.getType()) {
            case CHILD_ADDED:
                System.out.println("ADDED: " + path);
                break;
            case CHILD_UPDATED:
                System.out.println("UPDATED: " + path);
                break;
            case CHILD_REMOVED:
                System.out.println("REMOVED: " + path);
                break;
            case CONNECTION_SUSPENDED:
                System.out.println("SUSPENDED: " + path);
                break;
            case CONNECTION_RECONNECTED:
                System.out.println("RECONNECTED: " + path);
                break;
            case CONNECTION_LOST:
                System.out.println("LOST: " + path);
                break;
            case INITIALIZED:
                System.out.println("INITIALIZED: " + path);
                break;
        }
    };

    @Before
    public void setUp() throws Exception {
        client = new ZkClientFactoryBuilder()
                .zkAddresses("10.6.100.1:2181,10.6.100.2:2181,10.6.100.3:2181")
                .namespace("wfj-omni-channel-server")
                .build()
                .createClient();
        client2 = new ZkClientFactoryBuilder()
                .zkAddresses("10.6.100.1:2181,10.6.100.2:2181,10.6.100.3:2181")
                .build()
                .createClient();

        cache = new PathChildrenCache(client, "/", true);
        cache.getListenable().addListener(pathChildrenCacheListener);
        cache.start();

        cache2 = new PathChildrenCache(client2, "/wfj-omni-channel-server", true);
        cache2.getListenable().addListener(pathChildrenCacheListener);
        cache2.start();
    }

    @Test
    public void test() throws Exception {
        TimeUnit.SECONDS.sleep(3);
        client.create().creatingParentsIfNeeded().forPath("/testPath-000");
        TimeUnit.SECONDS.sleep(1);
        client.setData().forPath("/testPath-000", "test".getBytes("UTF-8"));
        TimeUnit.SECONDS.sleep(1);
        client.delete().forPath("/testPath-000");
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void test2() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(client2, "/wfj-omni-channel-server/log-analysis-71", true);
        cache.getListenable().addListener(pathChildrenCacheListener);
        cache.start();

        TimeUnit.SECONDS.sleep(10);
    }

    @Test
    public void test3() throws Exception {
        PathChildrenCache cache = new PathChildrenCache(client2, "/wfj-omni-channel-server", true);
        cache.getListenable().addListener((client, event) -> {
            String path = event.getData().getPath();
            switch (event.getType()) {
                case CHILD_ADDED:
                    PathChildrenCache cache1 = new PathChildrenCache(client2, path, true);
                    cache1.getListenable().addListener(pathChildrenCacheListener);
                    cache1.start();
                    break;
                case CHILD_UPDATED:
                    break;
                case CHILD_REMOVED:
                    break;
            }
        });
        cache.start();

        TimeUnit.SECONDS.sleep(10);
    }

    @After
    public void tearDown() throws Exception {
        cache.close();
        cache2.close();
        client.close();
        client2.close();
    }
}
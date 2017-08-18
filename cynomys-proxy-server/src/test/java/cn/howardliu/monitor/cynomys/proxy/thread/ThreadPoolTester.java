package cn.howardliu.monitor.cynomys.proxy.thread;

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <br>created at 17-8-18
 *
 * @author liuxh
 * @since 0.0.1
 */
@Ignore
public class ThreadPoolTester {
    @Test
    public void test() throws Exception {
        int count = 100;
        CountDownLatch latch = new CountDownLatch(count);
        ExecutorService executorService = Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
        List<Runnable> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int finalI = i;
            list.add(() -> {
                System.out.println(finalI);
                latch.countDown();
            });
        }
        for (Runnable runnable : list) {
            executorService.submit(runnable);
        }
        latch.await();
        executorService.shutdown();
    }
}

package cn.howardliu.monitor.cynomys.net;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public interface NetService {
    void start();

    void shutdown();

    boolean isStopped();
}

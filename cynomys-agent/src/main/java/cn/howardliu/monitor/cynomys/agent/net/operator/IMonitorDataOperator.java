package cn.howardliu.monitor.cynomys.agent.net.operator;

import cn.howardliu.monitor.cynomys.net.struct.Message;

/**
 * <br>created at 17-3-31
 *
 * @author liuxh
 * @since 0.0.1
 */
public interface IMonitorDataOperator {
    void start() throws Exception;

    void sendData(Message message);

    void handleData(Message message);
}

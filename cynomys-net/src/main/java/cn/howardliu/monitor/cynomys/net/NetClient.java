package cn.howardliu.monitor.cynomys.net;

import cn.howardliu.monitor.cynomys.net.struct.Message;

import java.util.List;

/**
 * <br>created at 17-8-9
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public interface NetClient extends NetService {
    void updateAddressList(List<String> addresses);

    List<String> getAddressList();

    boolean isChannelWriteable(final String address);

    boolean async(Message message) throws InterruptedException;
}

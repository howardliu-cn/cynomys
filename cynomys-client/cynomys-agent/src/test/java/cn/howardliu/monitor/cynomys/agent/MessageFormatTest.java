package cn.howardliu.monitor.cynomys.agent;

import org.junit.Test;

import java.text.MessageFormat;

/**
 * <br>created at 17-8-16
 *
 * @author liuxh
 * @since 0.0.1
 */
public class MessageFormatTest {
    @Test
    public void test() {
        Object[] arguments = {"Active"};
        String format = MessageFormat
                .format("'{'\"version\":\"1.0\", \"name\":\"cynomys-monitor-project\", \"desc\":\"cynomys-monitor-project, https://github.com/howardliu-cn/cynomys\", \"status\":\"{0}\"'}'",
                        arguments);
        System.out.println(format);
    }
}


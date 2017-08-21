package cn.howardliu.monitor.cynomys.net.struct;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Message {
    private Header header;
    private String body;

    public Header getHeader() {
        return header;
    }

    public Message setHeader(Header header) {
        this.header = header;
        return this;
    }

    public String getBody() {
        return body;
    }

    public Message setBody(String body) {
        this.body = body;
        return this;
    }

    @Override
    public String toString() {
        return "Message{" +
                "header=" + header +
                ", body=" + body +
                '}';
    }
}

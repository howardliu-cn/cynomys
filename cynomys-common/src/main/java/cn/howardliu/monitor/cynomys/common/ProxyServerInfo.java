package cn.howardliu.monitor.cynomys.common;

/**
 * <br>created at 17-7-17
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ProxyServerInfo {
    private String host;
    private int port;
    private int connectionCount;
    private String tag;
    private Status status = Status.UNAVAILABLE;
    private Type type = Type.UNKNOWN;

    public String getHost() {
        return host;
    }

    public ProxyServerInfo setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public ProxyServerInfo setPort(int port) {
        this.port = port;
        return this;
    }

    public int getConnectionCount() {
        return connectionCount;
    }

    public ProxyServerInfo setConnectionCount(int connectionCount) {
        this.connectionCount = connectionCount;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public ProxyServerInfo setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public ProxyServerInfo setStatus(Status status) {
        this.status = status;
        return this;
    }

    public Type getType() {
        return type;
    }

    public ProxyServerInfo setType(Type type) {
        this.type = type;
        return this;
    }

    public enum Type {
        UNKNOWN(0),
        LAN(1),
        WAN(2);

        private int code;

        Type(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }

    public enum Status {
        AVAILABLE(-1),
        UNAVAILABLE(1);

        private int code;

        Status(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }
    }
}

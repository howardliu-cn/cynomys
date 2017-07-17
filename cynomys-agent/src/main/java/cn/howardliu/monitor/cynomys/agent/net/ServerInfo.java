package cn.howardliu.monitor.cynomys.agent.net;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class ServerInfo {
    private String ip;
    private int port = -1;
    private int connectCount = -1;
    private ServerType type = ServerType.UNKNOWN;

    public ServerInfo(String ip, int port, ServerType type) {
        this(ip, port, 0, type);
    }

    public ServerInfo(String ip, int port, int connectCount, ServerType type) {
        this.ip = ip;
        this.port = port;
        this.connectCount = connectCount;
        this.type = type;
    }

    public String getTag() {
        return this.ip + this.port + this.type;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public ServerInfo setConnectCount(int connectCount) {
        this.connectCount = connectCount;
        return this;
    }

    public ServerType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerInfo that = (ServerInfo) o;
        return port == that.port && ip.equals(that.ip) && type == that.type;
    }

    @Override
    public int hashCode() {
        int result = ip.hashCode();
        result = 31 * result + port;
        result = 31 * result + type.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ServerInfo{" +
                "ip='" + ip + '\'' +
                ", port=" + port +
                ", connectCount=" + connectCount +
                ", type=" + type +
                '}';
    }

    public static enum ServerType {
        UNKNOWN,
        LAN,
        WAN
    }
}

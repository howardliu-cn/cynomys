package cn.howardliu.monitor.cynomys.net;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * <br>created at 17-8-8
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public final class NetHelper {
    public static final String OS_NAME = System.getProperty("os.name");
    private static final Logger logger = LoggerFactory.getLogger(NetHelper.class);
    private static boolean isLinuxPlatform = false;
    private static boolean isWindowsPlatform = false;

    static {
        if (OS_NAME != null && OS_NAME.toLowerCase().contains("linux")) {
            isLinuxPlatform = true;
        }

        if (OS_NAME != null && OS_NAME.toLowerCase().contains("windows")) {
            isWindowsPlatform = true;
        }
    }

    public static boolean isWindowsPlatform() {
        return isWindowsPlatform;
    }

    public static boolean isLinuxPlatform() {
        return isLinuxPlatform;
    }

    public static SocketAddress string2SocketAddress(final String address) {
        String[] s = address.split(":");
        return new InetSocketAddress(s[0], Integer.parseInt(s[1]));
    }

    public static String localAddress() {
        try {
            Enumeration<NetworkInterface> enumeration = NetworkInterface.getNetworkInterfaces();
            List<String> ipv4Result = new ArrayList<>();
            List<String> ipv6Result = new ArrayList<>();
            while (enumeration.hasMoreElements()) {
                final NetworkInterface networkInterface = enumeration.nextElement();
                final Enumeration<InetAddress> en = networkInterface.getInetAddresses();
                while (en.hasMoreElements()) {
                    final InetAddress address = en.nextElement();
                    if (!address.isLoopbackAddress()) {
                        String ipResult = normalizeHostAddress(address);
                        if (address instanceof Inet6Address) {
                            ipv6Result.add(ipResult);
                        } else {
                            ipv4Result.add(ipResult);
                        }
                    }
                }
            }

            if (!ipv4Result.isEmpty()) {
                for (String ip : ipv4Result) {
                    if (ip.startsWith("127.0") || ip.startsWith("192.168")) {
                        continue;
                    }
                    return ip;
                }
                return ipv4Result.get(ipv4Result.size() - 1);
            } else if (!ipv6Result.isEmpty()) {
                return ipv6Result.get(0);
            }

            return normalizeHostAddress(InetAddress.getLocalHost());
        } catch (SocketException | UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String normalizeHostAddress(final InetAddress localHost) {
        if (localHost instanceof Inet6Address) {
            return "[" + localHost.getHostAddress() + "]";
        } else {
            return localHost.getHostAddress();
        }
    }

    public static String remoteAddress(final Channel channel) {
        if (channel == null) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String address = remote == null ? "" : remote.toString();
        if (address.isEmpty()) {
            return "";
        } else if (address.contains("/")) {
            return address.substring(address.lastIndexOf("/") + 1);
        } else {
            return address;
        }
    }

    public static void closeChannel(final Channel channel) {
        if (channel == null) {
            return;
        }
        final String remote = remoteAddress(channel);
        channel.close().addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                logger.info("closeChannel: close the connection to remote address[{}] result: {}", remote,
                        future.isSuccess());
            } else {
                logger.warn("closeChannel: close the connection to remote address[{}] result: {}", remote,
                        future.isSuccess(), future.cause());
            }
        });
    }
}

package cn.howardliu.monitor.cynomys.common;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class CynomysVersion {
    public static final int CURRENT_VERSION = Version.V0_0_1_SNAPSHOT.ordinal();

    public static String getVersionDesc(int value) {
        try {
            Version v = Version.values()[value];
            return v.name();
        } catch (Exception ignored) {
        }
        return "HigherVersion";
    }

    public enum Version {
        V0_0_1_SNAPSHOT
    }
}

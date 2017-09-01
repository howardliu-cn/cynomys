package cn.howardliu.monitor.cynomys.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <br>created at 17-8-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class CynomysVersion {
    public static final int CURRENT_VERSION = Version.V0_0_1_SNAPSHOT.ordinal();
    public static final int CURRENT_VERSION_CODE = Version.V0_0_1_SNAPSHOT.code;

    private static final Logger logger = LoggerFactory.getLogger(CynomysVersion.class);

    public static String getVersionDesc(int value) {
        try {
            Version v = Version.values()[value];
            return v.name();
        } catch (Exception e) {
            logger.error("get Cynomys Version exception", e);
        }
        return "HigherVersion";
    }

    public enum Version {
        V0_0_1_SNAPSHOT(0x0000_0_0_1);

        int code;

        Version(int code) {
            this.code = code;
        }
    }
}

package cn.howardliu.monitor.cynomys.warn.exception;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum Flag {
    VALID("0"),
    INVALID("1");
    private final String code;

    Flag(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

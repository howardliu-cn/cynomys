package cn.howardliu.cynomys.warn.exception;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum ProcessStatus {
    UNPROCESSED("0"),
    INPROCESS("1"),
    PROCESSED("2");

    private final String code;

    ProcessStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

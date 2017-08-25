package cn.howardliu.cynomys.warn.log.exception;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public enum ErrorLevel {
    NONE("0"),// 初始化
    WARNING("1"),// 警告
    ERROR("2");// 异常

    private final String code;

    ErrorLevel(String code) {
        this.code = code;
    }

    public String getCode() {
        return this.code;
    }
}

package cn.howardliu.cynomys.warn.exception;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SysErrorInfo {
    private String code;
    private String desc;

    public SysErrorInfo() {
    }

    public SysErrorInfo(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}

package cn.howardliu.monitor.cynomys.agent.dto;

/**
 * <br>created at 17-9-1
 *
 * @author liuxh
 * @since 0.0.1
 */
public abstract class BaseInfo {
    protected String sysCode;
    protected String sysName;
    protected String sysIPS;
    protected String updateDate;

    public String getSysCode() {
        return sysCode;
    }

    public void setSysCode(String sysCode) {
        this.sysCode = sysCode;
    }

    public String getSysName() {
        return sysName;
    }

    public void setSysName(String sysName) {
        this.sysName = sysName;
    }

    public String getSysIPS() {
        return sysIPS;
    }

    public void setSysIPS(String sysIPS) {
        this.sysIPS = sysIPS;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }
}

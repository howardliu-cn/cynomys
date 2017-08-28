package cn.howardliu.cynomys.warn.exception;

import cn.howardliu.cynomys.warn.WarnLog;

import java.util.Date;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public class ExceptionLog implements WarnLog {
    private String errId;// 异常序列编号,生成规则,errLevel+flag+sysCode+bizCode+errCode+sysErrCode+timeStamp+四位随机数
    private String flag;// is valid   0:true  1:false
    private String errLevel;// 异常等级
    private String sysCode;// 系统编码
    private String bizCode;// 业务编码
    private String bizDesc;// 业务描述
    private String errCode;// 系统自定义异常编码
    private String errDesc;// 系统自定义异常描述
    private String sysErrCode;// 系统异常编码
    private String sysErrDesc;// 系统异常描述
    private String throwableDesc;// 异常详细信息
    private String createDate;// 创建日期
    private Date createTime;// 创建时间
    private String processStatus;// 该条信息统计进度

    public String getErrId() {
        return errId;
    }

    public ExceptionLog setErrId(String errId) {
        this.errId = errId;
        return this;
    }

    public String getFlag() {
        return flag;
    }

    public ExceptionLog setFlag(String flag) {
        this.flag = flag;
        return this;
    }

    public String getErrLevel() {
        return errLevel;
    }

    public ExceptionLog setErrLevel(String errLevel) {
        this.errLevel = errLevel;
        return this;
    }

    public void setErrLevelEnum(ErrorLevel errLevel) {
        this.errLevel = errLevel.getCode();
    }

    public String getSysCode() {
        return sysCode;
    }

    public ExceptionLog setSysCode(String sysCode) {
        this.sysCode = sysCode;
        return this;
    }

    public String getBizCode() {
        return bizCode;
    }

    public ExceptionLog setBizCode(String bizCode) {
        this.bizCode = bizCode;
        return this;
    }

    public String getBizDesc() {
        return bizDesc;
    }

    public ExceptionLog setBizDesc(String bizDesc) {
        this.bizDesc = bizDesc;
        return this;
    }

    public String getErrCode() {
        return errCode;
    }

    public ExceptionLog setErrCode(String errCode) {
        this.errCode = errCode;
        return this;
    }

    public String getErrDesc() {
        return errDesc;
    }

    public ExceptionLog setErrDesc(String errDesc) {
        this.errDesc = errDesc;
        return this;
    }

    public String getSysErrCode() {
        return sysErrCode;
    }

    public ExceptionLog setSysErrCode(String sysErrCode) {
        this.sysErrCode = sysErrCode;
        return this;
    }

    public String getSysErrDesc() {
        return sysErrDesc;
    }

    public ExceptionLog setSysErrDesc(String sysErrDesc) {
        this.sysErrDesc = sysErrDesc;
        return this;
    }

    public String getThrowableDesc() {
        return throwableDesc;
    }

    public ExceptionLog setThrowableDesc(String throwableDesc) {
        this.throwableDesc = throwableDesc;
        return this;
    }

    public String getCreateDate() {
        return createDate;
    }

    public ExceptionLog setCreateDate(String createDate) {
        this.createDate = createDate;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public ExceptionLog setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public String getProcessStatus() {
        return processStatus;
    }

    public ExceptionLog setProcessStatus(String processStatus) {
        this.processStatus = processStatus;
        return this;
    }

    public void setProcessStatusEnum(ProcessStatus processStatus) {
        this.processStatus = processStatus.getCode();
    }
}

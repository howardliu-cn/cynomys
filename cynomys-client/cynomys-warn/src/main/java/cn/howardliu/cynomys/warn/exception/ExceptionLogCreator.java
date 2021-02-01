package cn.howardliu.cynomys.warn.exception;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.text.DecimalFormat;
import java.util.Date;
import java.util.Random;

/**
 * <br>created at 17-8-28
 *
 * @author liuxh
 * @since 0.0.1
 */
public final class ExceptionLogCreator {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0000");
    private static final Random RANDOM = new Random();

    private ExceptionLogCreator() {
    }

    public static ExceptionLog create(String sysCode,
                                      String bizCode, String bizDesc,
                                      String errCode, String errDesc,
                                      String sysErrCode, String sysErrDesc,
                                      ErrorLevel errLevel, String throwableDesc) {
        ExceptionLog request = new ExceptionLog();
        if (sysErrCode == null || !sysErrCode.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("SysErrCode必须是3位数字");
        }
        request.setSysErrCode(sysErrCode);
        request.setSysErrDesc(sysErrDesc);
        request.setThrowableDesc(throwableDesc);
        if (StringUtils.isBlank(sysCode)) {
            throw new IllegalArgumentException("系统编码不能为空");
        }
        if (!sysCode.matches("^\\w{2,3}$")) {
            throw new IllegalArgumentException("系统必须是2位或3位数字或字母");
        }
        request.setSysCode(sysCode);
        Date date = new Date();
        request.setCreateDate(DateFormatUtils.format(date, "yyyy-MM-dd HH:mm:ss"));
        if (StringUtils.isBlank(bizCode)) {
            throw new IllegalArgumentException("业务编码不能为空");
        }
        if (!bizCode.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("业务编码必需为3位数字");
        }
        request.setBizCode(bizCode);
        if (StringUtils.isBlank(bizDesc)) {
            throw new IllegalArgumentException("业务描述不能为空");
        }
        request.setBizDesc(bizDesc);
        if (StringUtils.isBlank(errCode)) {
            throw new IllegalArgumentException("自定义错误编码不能为空");
        }
        if (!errCode.matches("^\\d{3}$")) {
            throw new IllegalArgumentException("自定义错误编码必需为3位数字");
        }
        request.setErrCode(errCode);
        if (StringUtils.isBlank(errDesc)) {
            throw new IllegalArgumentException("自定义错误编码不能为空");
        }
        request.setErrDesc(errDesc);
        request.setErrLevelEnum(errLevel);
        request.setFlag(Flag.VALID.getCode());
        request.setProcessStatusEnum(ProcessStatus.UNPROCESSED);
        request.setErrId(
                request.getErrLevel() + request.getFlag() + request.getSysCode() + request.getBizCode()
                        + request.getErrCode() + request.getSysErrCode()
                        + DateFormatUtils.format(new Date(), "yyyyMMddHHmmssSSS")
                        + DECIMAL_FORMAT.format(RANDOM.nextInt(10000)));
        return request;
    }
}

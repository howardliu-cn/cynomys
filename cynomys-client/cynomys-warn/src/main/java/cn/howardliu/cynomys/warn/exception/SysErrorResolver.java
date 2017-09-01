package cn.howardliu.cynomys.warn.exception;

import javax.xml.crypto.NoSuchMechanismException;
import javax.xml.ws.WebServiceException;
import java.awt.image.ImagingOpException;
import java.io.IOException;
import java.util.*;

/**
 * <br>created at 17-8-25
 *
 * @author liuxh
 * @since 0.0.1
 */
public class SysErrorResolver {
    public static final SysErrorInfo UNKNOWN_THROWABLE_SYS_ERROR = new SysErrorInfo("230", "其他异常");
    private final Map<Class<? extends Throwable>, SysErrorInfo> sysErrorMap;

    public SysErrorResolver() {
        LinkedHashMap<Class<? extends Throwable>, SysErrorInfo> map = new LinkedHashMap<>();
        this.initSysErrorCodeMap(map);
        this.sysErrorMap = Collections.unmodifiableMap(map);
    }

    protected void initSysErrorCodeMap(LinkedHashMap<Class<? extends Throwable>, SysErrorInfo> map) {
        map.put(ArithmeticException.class, new SysErrorInfo("001", "算术异常"));
        map.put(ArrayStoreException.class, new SysErrorInfo("002", "数组类型不兼容"));
        map.put(ClassCastException.class, new SysErrorInfo("003", "类型强制转换异常"));
        map.put(EmptyStackException.class, new SysErrorInfo("004", "堆栈为空"));
        map.put(SecurityException.class, new SysErrorInfo("005", "权限异常"));
        map.put(IllegalArgumentException.class, new SysErrorInfo("006", "方法传递了一个不合法或不正确的参数"));
        map.put(WebServiceException.class, new SysErrorInfo("007", "WebService异常"));
        map.put(NullPointerException.class, new SysErrorInfo("008", "空指针"));
        map.put(NoSuchMechanismException.class, new SysErrorInfo("009", "没有此类机制"));
        map.put(NoSuchElementException.class, new SysErrorInfo("010", "没有此类元素"));
        map.put(ImagingOpException.class, new SysErrorInfo("011", "图像操作异常"));
        map.put(UnsupportedOperationException.class, new SysErrorInfo("012", "UnsupportedOperationException"));
        map.put(IndexOutOfBoundsException.class, new SysErrorInfo("013", "使用了非法索引"));
        map.put(RuntimeException.class, new SysErrorInfo("200", "运行时异常"));
        map.put(IOException.class, new SysErrorInfo("210", "IO流异常"));
        map.put(Exception.class, new SysErrorInfo("220", "Exception"));
    }

    public SysErrorInfo errorOf(Throwable e) {
        SysErrorInfo error = null;
        for (Map.Entry<Class<? extends Throwable>, SysErrorInfo> entry : sysErrorMap.entrySet()) {
            if (e.getClass().isAssignableFrom(entry.getKey())) {
                error = entry.getValue();
                break;
            }
        }
        if (error == null) {
            error = UNKNOWN_THROWABLE_SYS_ERROR;
        }
        return error;
    }
}

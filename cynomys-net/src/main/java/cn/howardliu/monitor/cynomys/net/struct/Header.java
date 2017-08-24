package cn.howardliu.monitor.cynomys.net.struct;

import cn.howardliu.monitor.cynomys.common.Constant;

import java.util.concurrent.atomic.AtomicInteger;

import static cn.howardliu.monitor.cynomys.common.Constant.CRC_CODE;
import static cn.howardliu.monitor.cynomys.common.Constant.THIS_TAG;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Header {
    private static AtomicInteger requestId = new AtomicInteger(0);

    private int opaque = requestId.getAndIncrement();
    private int crcCode = CRC_CODE;
    private String tag = THIS_TAG;
    private String sysName = "";
    private String sysCode = "";
    private int length;
    private byte type;
    private byte code;
    private byte monitorPath = (byte) (Constant.onlyException ? 0 : 1);

    public int getOpaque() {
        return opaque;
    }

    public Header setOpaque(int opaque) {
        this.opaque = opaque;
        return this;
    }

    public int getCrcCode() {
        return crcCode;
    }

    public Header setCrcCode(int crcCode) {
        this.crcCode = crcCode;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Header setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public int getLength() {
        return length;
    }

    public Header setLength(int length) {
        this.length = length;
        return this;
    }

    public byte getType() {
        return type;
    }

    public Header setType(byte type) {
        this.type = type;
        return this;
    }

    public byte getCode() {
        return code;
    }

    public Header setCode(byte code) {
        this.code = code;
        return this;
    }

    public String getSysName() {
        return sysName;
    }

    public Header setSysName(String sysName) {
        this.sysName = sysName;
        return this;
    }

    public String getSysCode() {
        return sysCode;
    }

    public Header setSysCode(String sysCode) {
        this.sysCode = sysCode;
        return this;
    }

    public byte getMonitorPath() {
        return monitorPath;
    }

    public Header setMonitorPath(byte monitorPath) {
        this.monitorPath = monitorPath;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Header header = (Header) o;
        return crcCode == header.crcCode
                && tag.equals(header.tag)
                && sysCode.equals(header.sysCode)
                && sysName.equals(header.sysName);
    }

    @Override
    public int hashCode() {
        int result = crcCode;
        result = 31 * result + tag.hashCode();
        result = 31 * result + sysName.hashCode();
        result = 31 * result + sysCode.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Header{" +
                "opaque=" + opaque +
                ", crcCode=" + crcCode +
                ", tag='" + tag + '\'' +
                ", sysName='" + sysName + '\'' +
                ", sysCode='" + sysCode + '\'' +
                ", length=" + length +
                ", type=" + type +
                ", code=" + code +
                ", monitorPath=" + monitorPath +
                '}';
    }
}

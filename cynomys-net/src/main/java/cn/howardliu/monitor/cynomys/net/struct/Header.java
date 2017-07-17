package cn.howardliu.monitor.cynomys.net.struct;

import java.io.Serializable;

import static cn.howardliu.monitor.cynomys.common.Constant.CRC_CODE;
import static cn.howardliu.monitor.cynomys.common.Constant.THIS_TAG;

/**
 * <br>created at 17-5-11
 *
 * @author liuxh
 * @version 0.0.1
 * @since 0.0.1
 */
public class Header implements Serializable {
    private int crcCode = CRC_CODE;
    private String tag = THIS_TAG;
    private int length;
    private byte type;
    private String sysName = "";
    private int sysCode;

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

    public String getSysName() {
        return sysName;
    }

    public Header setSysName(String sysName) {
        this.sysName = sysName;
        return this;
    }

    public int getSysCode() {
        return sysCode;
    }

    public Header setSysCode(int sysCode) {
        this.sysCode = sysCode;
        return this;
    }

    @Override
    public String toString() {
        return "Header{" +
                "crcCode=" + crcCode +
                ", tag='" + tag + '\'' +
                ", length=" + length +
                ", type=" + type +
                ", sysName='" + sysName + '\'' +
                ", sysCode=" + sysCode +
                '}';
    }
}

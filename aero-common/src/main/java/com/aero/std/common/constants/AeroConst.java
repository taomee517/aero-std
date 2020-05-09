package com.aero.std.common.constants;

/**
 * @author 罗涛
 * @title AeroConst
 * @date 2020/5/9 11:17
 */
public class AeroConst {

    /**数据包最小长度:起始位(1) + 设备号(8) + 流水号（2） + 功能号（2） + 消息体属性(4) + 消息长度（2） + 校验码(2) + 停止位(1)*/
    public static final int  MIN_LENGTH = 22;

    public static final int MAX_LENGTH = 0xffff + 24;

    /**起始符和结束符*/
    public static final byte SIGN_CODE = 0x7e;
}

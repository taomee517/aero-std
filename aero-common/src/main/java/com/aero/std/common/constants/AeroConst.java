package com.aero.std.common.constants;

import com.aero.beans.constants.EnvType;

/**
 * @author 罗涛
 * @title AeroConst
 * @date 2020/5/9 11:17
 */
public class AeroConst {

    /**数据包最小长度:起始位(1) + 设备号(8) + 消息体属性(3) + 功能号（2） + 流水号（4） +  消息长度（2） + 校验码(2) + 停止位(1)*/
    public static final int  MIN_LENGTH = 23;

    public static final int MAX_LENGTH = 0xffff + 23;

    /**起始符和结束符*/
    public static final byte SIGN_CODE = 0x7e;

    /**转义符号*/
    public static final byte ESCAPE_SIGN = 0x7d;

    /**转义后的7D*/
    public static final byte[] ESCAPE_7D = {0x7d,0x00};

    /**转义后的7E*/
    public static final byte[] ESCAPE_7E = {0x7d,0x01};

    public static final String PROTOCOL_VERSION = "1";

    public static final EnvType ENV = EnvType.DEBUG;

}

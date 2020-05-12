package com.aero.beans.base;

import com.aero.beans.constants.*;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * @author 罗涛
 * @title Header
 * @date 2020/5/12 17:19
 */
@Data
public class Header {
    private String imei;
    private int serial;
    private FunctionType fun;
    private DataType dataType;
    private EnvType env;
    private EncryptType encrypt;
    private ValidateType validateType;
    private RequestType request;
    private boolean isSplitPack;
    private int total;
    private int currIndex;
    private int length;
    private byte[] crc;
    private ByteBuf content;
    private ByteBuf raw;
}

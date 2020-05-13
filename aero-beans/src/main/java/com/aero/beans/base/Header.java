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
    private String version;
    private RequestType request;
    private StatusCode statusCode;
    private DataType dataType;
    private EnvType env;
    private boolean isSplitPack;
    private EncryptType encrypt;
    private ValidateType validateType;
    private int total;
    private int currIndex;
    private FunctionType fun;
    private int serial;
    private int remoteSerial;
    private int length;
    private byte[] crc;
    private ByteBuf content;
    private ByteBuf raw;
}

package com.aero.std.common.constants;

import lombok.Getter;

/**
 * @author 罗涛
 * @title EncryptType
 * @date 2020/5/11 16:54
 */
@Getter
public enum  EncryptType {
    AES(0, "AES加密"),
    CRC(1, "CRC加密");

    private int code;
    private String desc;

    EncryptType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
}

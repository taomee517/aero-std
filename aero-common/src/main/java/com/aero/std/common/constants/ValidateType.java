package com.aero.std.common.constants;

import lombok.Getter;

/**
 * @author 罗涛
 * @title ValidateType
 * @date 2020/5/11 16:57
 */
@Getter
public enum ValidateType {
    CRC(0, "CRC校验"),
    LRC(1, "LRC校验"),
    BCC(2, "异或校验"),
    MD5(3, "MD5校验"),
    ;

    private int code;
    private String desc;

    ValidateType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
}

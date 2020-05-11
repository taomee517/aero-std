package com.aero.std.common.constants;

import lombok.Getter;

/**
 * @author 罗涛
 * @title DataType
 * @date 2020/5/11 17:02
 */
@Getter
public enum  DataType {
    TCP(0, "TCP"),
    UDP(1, "UDP"),
    ;

    private int code;
    private String desc;

    DataType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
}

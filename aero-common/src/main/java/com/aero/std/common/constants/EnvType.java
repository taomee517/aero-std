package com.aero.std.common.constants;

import lombok.Getter;

/**
 * @author 罗涛
 * @title EnvType
 * @date 2020/5/11 16:54
 */
@Getter
public enum EnvType {
    RELEASE(0, "正式环境"),
    DEBUG(1, "开发环境"),
    ;

    private int code;
    private String desc;

    EnvType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
}

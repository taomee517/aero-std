package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<Integer,EnvType> CODE_MAP = new HashMap<>();

    static {
        EnvType[] types = values();
        for(EnvType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static EnvType getEnvType(int code){
        return CODE_MAP.get(code);
    }
}

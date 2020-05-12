package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<Integer,EncryptType> CODE_MAP = new HashMap<>();

    static {
        EncryptType[] types = values();
        for(EncryptType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static EncryptType getDataType(int code){
        return CODE_MAP.get(code);
    }
}

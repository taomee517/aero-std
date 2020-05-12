package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

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

    private static Map<Integer,DataType> CODE_MAP = new HashMap<>();

    static {
        DataType[] types = values();
        for(DataType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static DataType getDataType(int code){
        return CODE_MAP.get(code);
    }
}

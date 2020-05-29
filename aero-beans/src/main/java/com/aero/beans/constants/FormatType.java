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
public enum FormatType {
    TLV(0, "TLV"),
    JSON(1, "JSON"),
    TEXT(2, "TEXT")
    ;

    private int code;
    private String desc;

    FormatType(int code, String desc){
        this.code = code;
        this.desc = desc;
    }

    private static Map<Integer, FormatType> CODE_MAP = new HashMap<>();

    static {
        FormatType[] types = values();
        for(FormatType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static FormatType getDataType(int code){
        return CODE_MAP.get(code);
    }
}

package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 罗涛
 * @title StatusCode
 * @date 2020/5/13 11:23
 */
@Getter
public enum StatusCode {
    REQUEST(0x00, "无-请求用"),
    ACCEPT(0x01, "已接受"),
    SUCCESS(0x02, "执行成功"),
    REFUSE(0x03, "拒绝写入"),
    UNSUPPORTED(0x04, "不支持的类型"),
    FAIL(0xff, "执行失败"),
    ;

    private int code;
    private String desc;

    StatusCode(int code,String desc){
        this.code = code;
        this.desc = desc;
    }

    private static Map<Integer,StatusCode> CODE_MAP = new HashMap<>();

    static {
        StatusCode[] types = values();
        for(StatusCode type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static StatusCode getStatusCode(int code){
        return CODE_MAP.get(code);
    }
}

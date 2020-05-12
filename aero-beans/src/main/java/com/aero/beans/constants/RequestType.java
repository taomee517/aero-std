package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 罗涛
 * @title RequestType
 * @date 2020/5/11 16:57
 */
@Getter
public enum RequestType {
    QUERY(0, "读请求"),
    QUERY_ACK(1, "读响应"),
    SETTING(2, "写请求"),
    SETTING_ACK(3, "写响应"),
    SUBSCRIBE(4, "订阅请求"),
    SUBSCRIBE_ACK(5, "订阅响应"),
    PUBLISH(6, "发布请求"),
    PUBLISH_ACK(7, "发布响应"),
    UNSUBSCRIBE(8, "取消订阅请求"),
    UNSUBSCRIBE_ACK(9, "取消订阅响应"),
    EXECUTE(10, "执行请求"),
    EXECUTE_ACK(11, "执行响应"),
    ;

    private int code;
    private String desc;

    RequestType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }


    private static Map<Integer,RequestType> CODE_MAP = new HashMap<>();

    static {
        RequestType[] types = values();
        for(RequestType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static RequestType getRequestType(int code){
        return CODE_MAP.get(code);
    }
}

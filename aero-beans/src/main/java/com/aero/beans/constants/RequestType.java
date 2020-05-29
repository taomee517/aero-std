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
    QUERY(0, "读请求",1),
    QUERY_ACK(1, "读响应",-1),
    SETTING(2, "写请求",3),
    SETTING_ACK(3, "写响应",-1),
    PUBLISH(4, "发布请求",5),
    PUBLISH_ACK(5, "发布响应",-1),
    EXECUTE(6, "执行请求",7),
    EXECUTE_ACK(7, "执行响应",-1),
    SUBSCRIBE(8, "订阅请求",9),
    SUBSCRIBE_ACK(9, "订阅响应",-1),
    UNSUBSCRIBE(10, "取消订阅请求",11),
    UNSUBSCRIBE_ACK(11, "取消订阅响应",-1),
    ;

    private int code;
    private String desc;
    private int ackCode;

    RequestType(int code,String desc,int ackCode){
        this.code = code;
        this.desc = desc;
        this.ackCode = ackCode;
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

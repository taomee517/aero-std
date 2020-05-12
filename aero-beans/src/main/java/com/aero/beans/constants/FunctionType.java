package com.aero.beans.constants;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author 罗涛
 * @title FunctionType
 * @date 2020/5/12 17:24
 */
@Getter
public enum FunctionType {
    HEART_BEAT(0, "心跳"),
    LOGIN(1, "登录"),
    TIME(2, "时间"),
    BATTERY(3, "电量"),
    DEVICE_INFO(4, "设备信息"),
    SERVER_URL(5, "上线地址"),
    INTERVAL(6, "频率"),
    CORE_DATA(7, "核心数据"),
    THRESHOLD(8, "阈值"),
    ALARM(9, "告警"),
    UPGRADE(10, "升级"),
    UPGRADE_SHARD(11, "升级分片"),
    UPGRADE_CRC(11, "升级分片校验"),
    UPGRADE_RESULT(11, "升级结果"),
    REBOOT(11, "重启"),
    DEBUG(11, "调试"),
    ERROR(11, "异常"),
    MULTI_SUBSCRIBE(11, "批量订阅"),
    ;

    private int code;
    private String desc;

    FunctionType(int code,String desc){
        this.code = code;
        this.desc = desc;
    }
    
    private static Map<Integer,FunctionType> CODE_MAP = new HashMap<>();

    static {
        FunctionType[] types = values();
        for(FunctionType type: types){
            CODE_MAP.put(type.getCode(),type);
        }
    }

    public static FunctionType getFunctionType(int code){
        return CODE_MAP.get(code);
    }
}

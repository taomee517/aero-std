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
    //交互类
    REGISTER(0x0000, "注册"),
    LOGIN(0x0001, "登录"),
    LOGOUT(0x0002, "登出"),
    HEART_BEAT(0x0100, "心跳"),

    //基础参数类
    TIME(0x1000, "时间校正"),
    DEVICE_INFO(0x1001, "设备信息"),
    BATTERY(0x1002, "电量"),
    SERVER_URL(0x1003, "上线地址"),
    INTERVAL(0x1004, "频率"),

    //核心数据类（传感器-采集）
    CORE_DATA(0x2000, "核心数据"),

    //参数管理类
    LOWER_THRESHOLD(0x3000, "下限阈值"),
    UPPER_THRESHOLD(0x3001, "上限阈值"),
    ALARM(0x3FFF, "告警"),

    //远程升级
    UPGRADE(0xFF00, "升级"),
    UPGRADE_SHARD(0xFF01, "升级分片"),
    UPGRADE_CRC(0xFF02, "升级分片校验"),
    UPGRADE_RESULT(0xFF03, "升级结果"),

    //设备远程运维
    REBOOT(0xFFF0, "重启"),
    DEBUG(0xFFFE, "调试"),
    ERROR(0xFFFF, "异常"),

    MULTI_SUBSCRIBE(0xA000, "批量订阅"),
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

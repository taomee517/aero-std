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
    REGISTER(0x0000, "注册", false, false, true, false),
    LOGIN(0x0001, "登录", false, false, true, false),
    LOGOUT(0x0002, "登出", false, false, true, false),
    HEART_BEAT(0x0100, "心跳",false, false, true, false),

    //基础参数类
    TIME(0x1000, "时间校正",true, true, true, false),
    DEVICE_INFO(0x1001, "设备信息",true, false, true, false),
    BATTERY(0x1002, "电量",true, false, true, false),
    SERVER_URL(0x1003, "上线地址",true, true, true, false),
    INTERVAL(0x1004, "频率",true, true, true, false),

    //核心数据类（传感器-采集）
    CORE_DATA(0x2000, "核心数据",false, false, true, true),

    //参数管理类
    LOWER_THRESHOLD(0x3000, "下限阈值",true, true, true, true),
    UPPER_THRESHOLD(0x3001, "上限阈值",true, true, true, true),
    ALARM(0x3FFF, "告警",false, false, true, false),

    EXCITATION_PARAMS(0x4000, "激振参数",true, true, true, false),


    //远程升级
    UPGRADE(0xFF00, "开始升级",false, false, true, true),
    //升级确认需要验证版本是否匹配，并上报设备最大可支持的分片大小
    UPGRADE_CONFIRM(0xFF01, "升级确认",false, false, true, true),
    //升级文件包含文件总大小，文件总分片， 文件总校验
    UPGRADE_FILE(0xFF02, "升级文件",false, false, true, true),
    UPGRADE_SHARD_REQ(0xFF03, "分片请求",false, false, true, true),
    UPGRADE_SHARD(0xFF03, "分片内容",false, false, true, true),
    UPGRADE_RESULT(0xFF04, "升级结果",false, false, true, false),

    //设备远程运维
    REBOOT(0xFFF0, "重启",false, false, false, true),
    PULL_CMD(0xFFFC, "拉取缓存指令",false, false, false, true),
    ECHO(0xFFFD, "回声测试",false, false, true, true),
    DEBUG(0xFFFE, "调试",false, false, false, true),
    ERROR(0xFFFF, "异常",false, false, true, false),

    MULTI_SUBSCRIBE(0xA000, "批量订阅",false, false, false, true),
    ;

    private int code;
    private String desc;
    private boolean supportQuery;
    private boolean supportSetting;
    private boolean supportPublish;
    private boolean supportExecute;

    FunctionType(int code, String desc, Boolean supportQuery, boolean supportSetting, boolean supportPublish, boolean supportExecute){
        this.code = code;
        this.desc = desc;
        this.supportQuery = supportQuery;
        this.supportSetting = supportSetting;
        this.supportPublish = supportPublish;
        this.supportExecute = supportExecute;
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

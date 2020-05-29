//package com.aero.beans.constants.discard;
//
//import lombok.Getter;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author 罗涛
// * @title ContentIdEnum
// * @date 2020/5/13 11:21
// */
//@Getter
//public enum TlvType {
//    REQ_SN(0x0000, 2, "请求流水号"),
//    STATUS_CODE(0xff00, 1, "响应状态码"),
//    TIMESTAMP(0x0001, 6, "时间戳"),
//    ;
//
//    private int code;
//    private int length;
//    private String desc;
//
//    TlvType(int code,int length,String desc){
//        this.code = code;
//        this.length = length;
//        this.desc = desc;
//    }
//
//    private static Map<Integer,TlvType> CODE_MAP = new HashMap<>();
//
//    static {
//        TlvType[] types = values();
//        for(TlvType type: types){
//            CODE_MAP.put(type.getCode(),type);
//        }
//    }
//
//    public static TlvType getTlvType(int code){
//        return CODE_MAP.get(code);
//    }
//}

//package com.aero.beans.constants.discard;
//
//import lombok.Getter;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @author 罗涛
// * @title ValidateType
// * @date 2020/5/11 16:57
// */
//@Getter
//public enum ValidateType {
//    CRC(0, "CRC校验"),
//    LRC(1, "LRC校验"),
//    BCC(2, "异或校验"),
//    MD5(3, "MD5校验"),
//    ;
//
//    private int code;
//    private String desc;
//
//    ValidateType(int code,String desc){
//        this.code = code;
//        this.desc = desc;
//    }
//
//
//    private static Map<Integer,ValidateType> CODE_MAP = new HashMap<>();
//
//    static {
//        ValidateType[] types = values();
//        for(ValidateType type: types){
//            CODE_MAP.put(type.getCode(),type);
//        }
//    }
//
//    public static ValidateType getValidateType(int code){
//        return CODE_MAP.get(code);
//    }
//}

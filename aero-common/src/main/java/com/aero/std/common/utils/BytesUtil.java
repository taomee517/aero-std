package com.aero.std.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author 罗涛
 * @title BytesUtil
 * @date 2020/5/8 10:52
 */
public class BytesUtil {
    public static byte[] int2TwoBytes(int value){
        byte[] bytes = new byte[]{(byte) (value >> 8), (byte) value};
        return bytes;
    }

    public static int twoBytes2Int(byte[] bytes){
        assert bytes.length == 2;
        return (bytes[0] & 0xFF) << 8 | (bytes[1] & 0xFF);
    }

    public static byte[] int2Bytes(int value){
        byte[] bytes = new byte[]{(byte) (value >> 24),(byte) (value >> 16),(byte) (value >> 8), (byte) value};
        return bytes;
    }

    public static int bytes2Int(byte[] bytes){
        assert bytes.length == 4;
        return (bytes[0] & 0xFF) << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
    }

    public static long bytes2Long(byte[] bytes){
        long value = 0L;
//        int len = bytes.length;
//        for (int i = 0; i < len; i++) {
//            int b = bytes[i] & 0xff;
//            int offset = 8*(len-i-1);
//            value |= (b << offset);
//        }
        String hex = bytes2Hex(bytes);
        value = Long.parseLong(hex,16);
        return value;
    }

    public static byte[] utc2Bytes(long utc){
        byte[] bytes = new byte[]{(byte) (utc >> 40),(byte) (utc >> 32),(byte) (utc >> 24),(byte) (utc >> 16),(byte) (utc >> 8), (byte) utc};
        return bytes;
    }

    public static String bytes2HexWithBlank(byte[] bytes, boolean withBlank){

        StringBuilder sb = new StringBuilder();
        for(byte b: bytes){
            //不够两位的，0来填充
            String singleHex = ZeroFillUtil.getZeroFilledHex(b & 0xff, 2);
            sb.append(singleHex);
            if(withBlank){
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public static String bytes2Hex(byte[] bytes){
        return bytes2HexWithBlank(bytes,false);
    }

    public static byte[] hex2Bytes(String hex){
        if(StringUtils.countMatches(hex," ")>0){
            hex = StringUtils.replace(hex, " ", "");
        }
        byte[] bc = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length() / 2; ++i) {
            String tc = hex.substring(i * 2, (i + 1) * 2);
            int a = Integer.parseInt(tc, 16);
            bc[i] = (byte) a;
        }
        return bc;
    }

    public static boolean arrayEqual(byte[] arr1, byte[] arr2){
        if(arr1.length!=arr2.length){
            return false;
        }
        if(arr1==null && arr2 == null){
            return true;
        }
        for(int i=0;i<arr1.length;i++){
            byte b1 = arr1[i];
            byte b2 = arr2[i];
            if(b1!=b2){
                return false;
            }
        }
        return true;
    }

    public static byte[] imei2Bytes(String imei){
        Long longVal = Long.parseLong(imei,16);
        byte[] result = new byte[8];
        String imeiHex = ZeroFillUtil.zeroFillStr(imei,16);
        for(int i=0;i<imeiHex.length();i+=2){
            String sHex = imeiHex.substring(i,i+2);
            int b = Integer.parseInt(sHex,16);
            result[i/2] = (byte) b;
        }
        return result;
    }

//    public static String bytes2Imei(byte[] bytes){
//        StringBuilder sb = new StringBuilder();
//        for(byte b: bytes){
//            String ss = Integer.toHexString(b);
//            ss = ZeroFillUtil.zeroFillStr(ss,2);
//            sb.append(ss);
//        }
//        return StringUtils.removeStart(sb.toString(),"0");
//    }

    public static String bytes2Imei(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        byte[] var2 = bytes;
        int var3 = bytes.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            String ss = Integer.toHexString(b&0xff);
            ss = ZeroFillUtil.zeroFillStr(ss, 2);
            sb.append(ss);
        }

        return StringUtils.removeStart(sb.toString(), "0");
    }

    public static String toShowString(byte[] bytes){
        int lastIndex = bytes.length-1;
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < lastIndex; i++) {
            sb.append(bytes[i]);
            sb.append(",");
        }
        sb.append(bytes[lastIndex]);
        sb.append("]");
        return sb.toString();
    }

    public static void main(String[] args) {
        String imei = "813463346541368";
        byte[] result = imei2Bytes(imei);
        System.out.println(toShowString(result));
        System.out.println(bytes2Hex(result));

        String testImei = "00813463346541368";
        testImei = StringUtils.removeStart(testImei,"0");
        System.out.println(testImei);
    }
}

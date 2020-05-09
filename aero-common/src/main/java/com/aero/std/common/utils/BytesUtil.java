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
}

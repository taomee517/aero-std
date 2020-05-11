package com.aero.std.common.utils;

import io.netty.buffer.ByteBuf;

/**
 * @author 罗涛
 * @title ValidateUtil
 * @date 2020/5/11 16:51
 */
public class ValidateUtil {
    public static byte[] calCrc(byte[] data){
        byte[] crc = new byte[2];
        int CRC = 0x0000ffff;
        int POLYNOMIAL = 0x0000a001;

        int i, j;
        for (i = 0; i < data.length; i++) {
            CRC ^= ((int) data[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        crc[0] = (byte) (CRC >> 8 & 0xff);
        crc[1] = (byte) (CRC & 0xff);
        return crc;
    }

    public static byte[] calCrc(ByteBuf content){
        byte[] bytes = new byte[content.readableBytes()];
        content.readBytes(bytes);
        return calCrc(bytes);
    }
}

package com.aero.std.common.utils;

import com.aero.std.common.constants.AeroConst;
import io.netty.buffer.ByteBuf;

import static com.aero.std.common.constants.AeroConst.SIGN_CODE;

/**
 * @author 罗涛
 * @title EscapeUtil
 * @date 2020/5/12 16:11
 * @desc 这种反转义写法，算法的复杂度比AeroParser中的方法要高，暂不使用，仅保留代码
 */
public class EscapeUtil {

    public static void unescape(ByteBuf in){
        int srcRdx = in.readerIndex();
        byte[] temp = new byte[2];
        while (in.readableBytes()>1){
            temp[0] = in.readByte();
            temp[1] = in.getByte(in.readerIndex());
            if(BytesUtil.arrayEqual(temp, AeroConst.ESCAPE_7D)){
                leftMove(in);
                continue;
            }
            if(BytesUtil.arrayEqual(temp,AeroConst.ESCAPE_7E)){
                in.setByte(in.readerIndex()-1, SIGN_CODE);
                leftMove(in);
                continue;
            }
        }
        in.readerIndex(srcRdx);
    }

    private static void leftMove(ByteBuf frame){
        byte[] bytes = new byte[frame.readableBytes()-1];
        int rdx = frame.readerIndex();
        int end = frame.capacity();
        frame.getBytes(rdx+1,bytes);
        for(int i=rdx;i<end-1;i++){
            byte b = bytes[i-rdx];
            frame.setByte(i, b);
        }
        frame.capacity(end-1);
    }
}

package com.aero.std.common.sdk;

import com.aero.std.common.utils.BytesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ByteProcessor;
import lombok.extern.slf4j.Slf4j;

import static com.aero.std.common.constants.AeroConst.MIN_LENGTH;
import static com.aero.std.common.constants.AeroConst.SIGN_CODE;

/**
 * @author 罗涛
 * @title AeroParser
 * @date 2020/5/9 11:14
 */
@Slf4j
public class AeroParser {
    public static ByteBuf split(ByteBuf in){
        int readableLen = in.readableBytes();
        if (readableLen < MIN_LENGTH) {
            return null;
        }
        int startSignIndex = in.forEachByte(new ByteProcessor.IndexOfProcessor(SIGN_CODE));
        if(startSignIndex==-1){
            return null;
        }
        //将readerIndex置为起始符下标+1
        //因为起始符结束符是一样的，如果不往后移一位，下次到的还是起始下标
        in.readerIndex(startSignIndex + 1);

        //找到第一个报文结束符的下标
        int endSignIndex = in.forEachByte(new ByteProcessor.IndexOfProcessor(SIGN_CODE));
        if(endSignIndex == -1 || endSignIndex < startSignIndex){
            in.readerIndex(startSignIndex);
            return null;
        }


        //计算报文的总长度
        //此处不能去操作writerIndex,否则只能截取到第一条完整报文
        int length = endSignIndex + 1 - startSignIndex;

        //如果长度还小于最小长度，就丢掉这条消息
        if(length < MIN_LENGTH){
            byte[] errMsg = new byte[length];
            for(int i= startSignIndex; i< (endSignIndex + 1); i++){
                int errIndex = i-startSignIndex;
                errMsg[errIndex] = in.getByte(i);
            }
            log.error("异常消息：{}", BytesUtil.bytes2HexWithBlank(errMsg, true));
            in.readerIndex(endSignIndex);
            return null;
        }

        //将报文内容写入符串，并返回
        byte[] data = new byte[length];
        in.readerIndex(startSignIndex);
        in.readBytes(data);
        return Unpooled.wrappedBuffer(data);
    }
}

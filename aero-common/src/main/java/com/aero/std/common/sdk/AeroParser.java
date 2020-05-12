package com.aero.std.common.sdk;

import com.aero.beans.base.Header;
import com.aero.beans.constants.*;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.utils.BytesUtil;
import com.aero.std.common.utils.ValidateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ByteProcessor;
import org.apache.commons.lang3.StringUtils;

import static com.aero.std.common.constants.AeroConst.*;

/**
 * @author 罗涛
 * @title AeroParser
 * @date 2020/5/9 11:14
 */
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

            String hexMsg = BytesUtil.bytes2HexWithBlank(errMsg, true);
//            log.error("异常消息：{}", hexMsg);
            System.out.println("异常消息：" + hexMsg);
            in.readerIndex(endSignIndex);
            return null;
        }

        //将报文内容写入符串，并返回
        byte[] data = new byte[length];
        in.readerIndex(startSignIndex);
        in.readBytes(data);
        return Unpooled.wrappedBuffer(data);
    }



    public static void unescape(ByteBuf in){
        int srcRdx = in.readerIndex();
        ByteBuf cp = in.readBytes(in.readableBytes());
        in.setIndex(srcRdx,srcRdx);
        try {
            boolean skip = false;
            byte[] temp = new byte[2];
            while (cp.readableBytes()>1){
                byte curr = cp.readByte();
                temp[0] = curr;
                temp[1] = cp.getByte(cp.readerIndex());
                if(skip){
                    in.capacity(in.capacity()-1);
                    skip = false;
                    continue;
                }
                if(BytesUtil.arrayEqual(temp,AeroConst.ESCAPE_7D)){
                    in.writeByte(AeroConst.ESCAPE_SIGN);
                    skip = true;
                    continue;
                }
                if(BytesUtil.arrayEqual(temp,AeroConst.ESCAPE_7E)){
                    in.writeByte(SIGN_CODE);
                    skip = true;
                    continue;
                }
                in.writeByte(curr);
            }
            in.writeByte(temp[1]);
        } finally {
            cp.release();
        }
    }


    public static ByteBuf escape(ByteBuf in){
        ByteBuf out = Unpooled.buffer(in.capacity()*2);
        int capacity = in.capacity();
        out.writeByte(SIGN_CODE);
        in.readerIndex(1);
        while (in.readableBytes()>1){
            byte curr = in.readByte();
            if(curr == ESCAPE_SIGN){
                capacity ++;
                out.capacity(capacity);
                out.writeBytes(ESCAPE_7D);
                continue;
            }
            if(curr == SIGN_CODE){
                capacity ++;
                out.capacity(capacity);
                out.writeBytes(ESCAPE_7E);
                continue;
            }
            out.writeByte(curr);
        }
        out.writeByte(SIGN_CODE);
        return out;
    }


    public static boolean validate(ByteBuf in){
        int length = in.readableBytes();
        if(length < MIN_LENGTH){
            return false;
        }
        //排除首，尾和校验码部分
        byte[] validatingContent = new byte[length-4];
        in.readerIndex(1);
        in.readBytes(validatingContent);

        byte[] srcCrc = new byte[2];
        in.readBytes(srcCrc);

        byte[] calCrc = ValidateUtil.calCrc(validatingContent);
        return BytesUtil.arrayEqual(srcCrc,calCrc);
    }

    public static Header parseHeader(ByteBuf in){
        Header header = new Header();
        //帧头
        byte start = in.readByte();
        //imei
        byte[] srcImei = new byte[8];
        in.readBytes(srcImei);
        String imei = BytesUtil.bytes2Imei(srcImei);
        //流水号
        int serial = in.readShort();
        //功能号
        int funId = in.readShort();
        FunctionType func = FunctionType.getFunctionType(funId);
        //属性
        int attr = in.readInt();
        //传输类型
        int dataTypeCode = attr >> 30 & 3;
        DataType dataType = DataType.getDataType(dataTypeCode);
        //环境
        int envCode = attr >> 22 & 3;
        EnvType env = EnvType.getEnvType(envCode);
        //加密方式
        int encrypCode = attr >> 19 & 7;
        EncryptType encryptType = EncryptType.getDataType(encrypCode);
        //校验方式
        int validateTypeCode = attr >> 16 & 7;
        ValidateType validateType = ValidateType.getValidateType(validateTypeCode);
        //请求方式
        int requestCode = attr >> 12 & 0xf;
        RequestType requestType = RequestType.getRequestType(requestCode);
        //是否分包
        boolean isSplitPack = (attr >> 11 & 1) == 1;
        if(isSplitPack){
            int splitPackInfo = in.readInt();
            int total = splitPackInfo & 0xff;
            int currIndex = splitPackInfo >> 16 & 0xff;
            header.setTotal(total);
            header.setCurrIndex(currIndex);
        }
        //协议版本
        int srcVer = attr & 7;
        int bigVer = srcVer >> 4 & 0xf;
        int smallVer = srcVer & 0xf;
        String version = StringUtils.join(bigVer, ".", smallVer);
        //消息长度
        int length = in.readShort();
        //消息内容
        ByteBuf content = in.readRetainedSlice(length);
        //校验
        byte[] crc = new byte[2];
        in.readBytes(crc);
        //帧尾
        byte end = in.readByte();

        header.setImei(imei);
        header.setSerial(serial);
        header.setFun(func);
        header.setDataType(dataType);
        header.setEnv(env);
        header.setEncrypt(encryptType);
        header.setValidateType(validateType);
        header.setRequest(requestType);
        header.setSplitPack(isSplitPack);
        header.setLength(length);
        header.setContent(content);
        return header;
    }


    public static String buffer2Hex(ByteBuf frame){
        try {
            byte[] bytes = new byte[frame.readableBytes()];
            frame.readBytes(bytes);
            return BytesUtil.bytes2HexWithBlank(bytes,true);
        } finally {
            frame.resetReaderIndex();
        }
    }


    public static void main(String[] args) {
//        String src = "7e 08 13 46 33 46 54 13 68 00 7d 00 7d 01 00 03 00 10 00 00 09 42 7e";
        String src = "7e 08 13 46 33 46 54 13 68 00 7d 7e 00 03 00 10 00 00 09 42 7e";
        src = StringUtils.replace(src," ", "");
        byte[] bytes = BytesUtil.hex2Bytes(src);
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
//        unescape(buffer);
//        String newHex = buffer2Hex(buffer);
//        System.out.println("反转义后的结果：" + newHex);

        ByteBuf out = escape(buffer);
        String newHex = buffer2Hex(out);
        System.out.println("转义后的结果：" + newHex);
    }
}
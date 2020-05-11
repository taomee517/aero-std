package com.aero.std.common.sdk;

import com.aero.std.common.constants.*;
import com.aero.std.common.utils.BytesUtil;
import com.aero.std.common.utils.ValidateUtil;
import com.aero.std.common.utils.VersionUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

/**
 * @author 罗涛
 * @title AeroMsgBuilder
 * @date 2020/5/11 16:03
 */
public class AeroMsgBuilder {

    public static ByteBuf buildMessage(String imei, int serial, int funId, byte[] attr, ByteBuf content){

        ByteBuf buffer = Unpooled.buffer();
        //帧头
        buffer.writeByte(AeroConst.SIGN_CODE);
        //设备号
        buffer.writeBytes(BytesUtil.imei2Bytes(imei));
        //流水号
        buffer.writeBytes(BytesUtil.int2TwoBytes(serial));
        //功能号
        buffer.writeBytes(BytesUtil.int2TwoBytes(funId));
        //属性
        buffer.writeBytes(attr);
        //长度
        int length = Objects.nonNull(content)?content.readableBytes():0;
        buffer.writeBytes(BytesUtil.int2TwoBytes(length));
        //内容
        if (Objects.nonNull(content)) {
            buffer.writeBytes(content);
        }
        //校验码
        ByteBuf validatingContent = Unpooled.buffer(buffer.readableBytes()-1);
        buffer.getBytes(1,validatingContent);
        byte[] crc = ValidateUtil.calCrc(validatingContent);
        buffer.writeBytes(crc);
        //帧尾
        buffer.writeByte(AeroConst.SIGN_CODE);
        return buffer;
    }

    public static byte[] buildAttribute(int dataTypeCode, int envCode,
                                 int encryptCode, int validateTypeCode,
                                 int requestCode, boolean splitPack,
                                 String version, int total, int current){
        byte[] bytes = new byte[4];
        if(splitPack){
            bytes = new byte[6];
        }
        int attr = dataTypeCode & 3 << 30;
        attr |= envCode & 3 << 22;
        attr |= encryptCode & 7 << 19;
        attr |= validateTypeCode & 7 << 16;
        attr |= requestCode & 0xf << 12;
        int packCode = splitPack?1:0;
        attr |= packCode << 11;
        int versionCode = 0;
        try {
            versionCode = VersionUtil.version2Byte(version);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        attr |= versionCode & 0xff;
        byte[] attrBytes = BytesUtil.int2Bytes(attr);
        if(!splitPack){
            bytes = attrBytes;
            return bytes;
        }else {
            System.arraycopy(attrBytes,0,bytes,0, 4);
            bytes[4] = ((byte) total);
            bytes[5] = ((byte) current);
            return bytes;
        }

    }

    public static byte[] buildAttribute(DataType dataType, EnvType envType,
                                        EncryptType encryptType, ValidateType validateType,
                                        RequestType requestType, boolean splitPack,
                                        String version, int total, int current) {
        return buildAttribute(dataType.getCode(),envType.getCode(),encryptType.getCode(),
                validateType.getCode(),requestType.getCode(),splitPack,version,total,current);
    }


}

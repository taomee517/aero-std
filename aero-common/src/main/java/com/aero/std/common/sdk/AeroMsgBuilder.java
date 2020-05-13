package com.aero.std.common.sdk;

import com.aero.beans.base.Header;
import com.aero.beans.constants.*;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.utils.BytesUtil;
import com.aero.std.common.utils.SnUtil;
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

    public static ByteBuf buildMessage(String imei, FunctionType functionType, byte[] attr, ByteBuf content){
        int serial = SnUtil.getSn(imei);
        int funId = functionType.getCode();
        return buildMessage(imei,serial,0, funId,attr,content);
    }

    public static ByteBuf buildMessage(String imei, FunctionType functionType, int remoteSerial, byte[] attr, ByteBuf content){
        int serial = SnUtil.getSn(imei);
        int funId = functionType.getCode();
        return buildMessage(imei,serial,remoteSerial, funId,attr,content);
    }

    public static ByteBuf buildMessage(String imei, int localSerial, int remoteSerial, int funId, byte[] attr, ByteBuf content){

        ByteBuf buffer = Unpooled.buffer();
        //帧头
        buffer.writeByte(AeroConst.SIGN_CODE);
        //设备号
        buffer.writeBytes(BytesUtil.imei2Bytes(imei));
        //属性
        buffer.writeBytes(attr);
        //功能号
        buffer.writeBytes(BytesUtil.int2TwoBytes(funId));
        //流水号-本端
        buffer.writeBytes(BytesUtil.int2TwoBytes(localSerial));
        //流水号-对端
        buffer.writeBytes(BytesUtil.int2TwoBytes(remoteSerial));
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

    public static byte[] buildAttribute(String version, int statusCode,int requestCode,int dataTypeCode, int envCode,
                                        boolean splitPack,int encryptCode, int validateTypeCode,int total, int current){
        byte[] bytes = new byte[4];
        if(splitPack){
            bytes = new byte[6];
        }
        int versionCode = 0;
        try {
            versionCode = VersionUtil.version2Byte(version);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        long attr = (versionCode & 0xff) << 24;
        attr |= (statusCode & 0xff) << 16;
        attr |= (requestCode & 0xf) << 12;
        attr |= (dataTypeCode & 0xf) << 8;
        attr |= (envCode & 1) << 7;
        int packCode = splitPack?1:0;
        attr |= packCode << 6;
        attr |= (encryptCode & 3) << 2;
        attr |= (validateTypeCode & 3);
        byte[] attrBytes = BytesUtil.int2Bytes(((int) attr));
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

    public static byte[] buildAttribute(String version, StatusCode status, RequestType requestType, DataType dataType,
                                        EnvType envType, boolean splitPack,EncryptType encryptType,
                                        ValidateType validateType,int total, int current) {
        return buildAttribute(version,status.getCode(),requestType.getCode(),dataType.getCode(),envType.getCode(),
                splitPack,encryptType.getCode(),validateType.getCode(),total,current);
    }

    public static byte[] buildAttribute(String version, StatusCode status, RequestType requestType, DataType dataType,
                                        EnvType envType, boolean splitPack,EncryptType encryptType,
                                        ValidateType validateType) {
        return buildAttribute(version,status.getCode(), requestType.getCode(),dataType.getCode(),envType.getCode(),
                splitPack,encryptType.getCode(),validateType.getCode(),0,0);
    }

    /**
     * 创建回复内容
     * @param header
     * @return
     */
    public static ByteBuf buildResponse(Header header, StatusCode status){
        String imei = header.getImei();
        int remoteSerial = header.getSerial();
        FunctionType functionType = header.getFun();
        RequestType requestType = header.getRequest();
        int ackCode = requestType.getAckCode();
        if(ackCode==-1){
            return null;
        }
        byte[] attr = buildAttribute(header.getVersion(),status,RequestType.getRequestType(ackCode), DataType.TLV,EnvType.DEBUG,false, header.getEncrypt(),
                header.getValidateType());
        ByteBuf content = null;
//        ByteBuf content = Unpooled.buffer();
//        content.writeBytes(BytesUtil.int2TwoBytes(TlvType.REQ_SN.getCode()));
//        content.writeBytes(BytesUtil.int2TwoBytes(TlvType.REQ_SN.getLength()));
//        content.writeBytes(BytesUtil.int2TwoBytes(srcSerial));
//        content.writeBytes(BytesUtil.int2TwoBytes(TlvType.STATUS_CODE.getCode()));
//        content.writeBytes(BytesUtil.int2TwoBytes(TlvType.STATUS_CODE.getLength()));
//        content.writeByte(StatusCode.ACCEPT.getCode());
//        content.capacity(content.readableBytes());
        ByteBuf buf = buildMessage(imei,functionType,remoteSerial,attr,content);
        return buf;
    }








}

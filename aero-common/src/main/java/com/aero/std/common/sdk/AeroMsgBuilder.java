package com.aero.std.common.sdk;

import com.aero.beans.base.Header;
import com.aero.beans.constants.*;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.utils.BytesUtil;
import com.aero.std.common.utils.SnUtil;
import com.aero.std.common.utils.ValidateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Objects;

/**
 * @author 罗涛
 * @title AeroMsgBuilder
 * @date 2020/5/11 16:03
 */
public class AeroMsgBuilder {

    /**
     * 创建请求消息，远端流水号为0
     * @param imei 设备号
     * @param functionType  功能号
     * @param attr 属性
     * @param content 消息内容
     * @return
     */
    public static ByteBuf buildRequestMessage(String imei, FunctionType functionType, byte[] attr, ByteBuf content){
        int serial = SnUtil.getSn(imei);
        int funId = functionType.getCode();
        return buildMessage(imei,serial,0, funId,attr,content);
    }

    public static ByteBuf buildAckMessage(String imei, FunctionType functionType, int remoteSerial, byte[] attr, ByteBuf content){
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
        //内容 => 还未作加密 TODO
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

    public static byte[] buildAttribute(String version, int statusCode, int envCode, int dataTypeCode, int requestCode){
        byte[] attrBytes = new byte[3];
        int versionCode = Integer.valueOf(version);
        attrBytes[0] = ((byte) versionCode);
        attrBytes[1] = ((byte) statusCode);
        byte attr = 0;
        attr |= (envCode & 1) << 7;
        attr |= (dataTypeCode & 0x7) << 4;
        attr |= requestCode & 0xf;
        attrBytes[2] = attr;
        return attrBytes;
    }

    public static byte[] buildAttribute(String version, StatusCode status, EnvType envType, FormatType formatType, RequestType requestType) {
        return buildAttribute(version,status.getCode(),envType.getCode(), formatType.getCode(),requestType.getCode());
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
        RequestType ackType = RequestType.getRequestType(requestType.getAckCode());
        int ackCode = requestType.getAckCode();
        if(ackCode==-1){
            return null;
        }
        byte[] attr = buildAttribute(header.getVersion(),status,header.getEnv(), FormatType.TLV,ackType);
        ByteBuf content = null;
        if(FunctionType.TIME.equals(header.getFun())){
            content = Unpooled.buffer(10);
            content.writeShort(1);
            content.writeShort(6);
            byte[] timeBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
            content.writeBytes(timeBytes);
        }
        ByteBuf buf = buildAckMessage(imei,functionType,remoteSerial,attr,content);
        return buf;
    }








}

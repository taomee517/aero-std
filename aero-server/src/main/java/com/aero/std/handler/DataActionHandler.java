package com.aero.std.handler;

import com.aero.beans.base.Header;
import com.aero.beans.base.Message;
import com.aero.beans.constants.*;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.sdk.AeroParser;
import com.aero.std.common.utils.BytesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title DataActionHandler
 * @date 2020/5/13 10:43
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class DataActionHandler extends ChannelDuplexHandler {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.warn("与设备建立连接，remote:{}", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("与设备断开连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message data = ((Message) msg);
//        RequestType req = data.getHeader().getRequest();
        Header header = data.getHeader();
        String imei = header.getImei();
        ByteBuf resp = null;
        FunctionType func = data.getHeader().getFun();
        log.info("收到设备消息，消息类型：{}", func.getDesc());
        switch (func){
            case HEART_BEAT:
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
                break;
            case LOGIN:
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.SUCCESS);
                break;
            case TIME:
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
                long dutc = data.getBodies().get(0).getUtc();
                long utc = System.currentTimeMillis();
                long offset = utc - dutc;
                log.info("时间差：{}", offset);
                if(offset > 10000){
                    log.warn("设备与平台的时间差较大，下发时间校正指令！");
                    byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.REFUSE, RequestType.SETTING, DataType.TLV, EnvType.DEBUG,false,EncryptType.CRC,ValidateType.CRC);
                    ByteBuf content = Unpooled.buffer();
                    byte[] utcBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
                    content.writeBytes(BytesUtil.int2TwoBytes(2));
                    content.writeBytes(BytesUtil.int2TwoBytes(utcBytes.length));
                    content.writeBytes(utcBytes);
                    content.capacity(content.readableBytes());
                    ByteBuf timeAdjustCmd = AeroMsgBuilder.buildMessage(imei,FunctionType.TIME,attr, content);
                    ctx.writeAndFlush(timeAdjustCmd);
                }
                break;
            default:
                break;
        }
        if (resp!=null) {
            String hexResp = AeroParser.buffer2Hex(resp);
            log.info("响应设备：{}", hexResp);
            ctx.writeAndFlush(resp);
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleEvt = ((IdleStateEvent) evt);
            switch (idleEvt.state()){
                case READER_IDLE:
                    log.info("server 读超时！");
                    break;
                case WRITER_IDLE:
                    log.info("server 写超时！");
                    break;
                case ALL_IDLE:
                    log.info("server 全超时！");
                    break;
                default: break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("DataActionHandler 发生异常：{}", cause);
        ctx.close();
    }
}

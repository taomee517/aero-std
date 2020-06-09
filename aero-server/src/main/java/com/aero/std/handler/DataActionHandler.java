package com.aero.std.handler;

import com.aero.beans.base.Body;
import com.aero.beans.base.Header;
import com.aero.beans.base.Message;
import com.aero.beans.constants.FormatType;
import com.aero.beans.constants.FunctionType;
import com.aero.beans.constants.RequestType;
import com.aero.beans.constants.StatusCode;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.Date;

/**
 * @author 罗涛
 * @title DataActionHandler
 * @date 2020/5/13 10:43
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class DataActionHandler extends ChannelDuplexHandler {

    @Autowired
    RedisTemplate<String, String> redisTemplate;

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
            case REGISTER:
                long registrerUtc = data.getBodies().get(0).getDeviceUtc();
                String factor = StringUtils.join(imei,registrerUtc);
                String md5 = DigestUtils.md5DigestAsHex(factor.getBytes());
                String pwd = md5.substring(md5.length()-8);
                redisTemplate.opsForValue().set("Register:" + header.getImei(), pwd);
                log.info("设备{}注册，计算并保存登录口令：{}",header.getImei(), pwd);
                byte[] loginPwd = pwd.getBytes();
                byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.SUCCESS, AeroConst.ENV, FormatType.TLV,RequestType.PUBLISH_ACK);
                ByteBuf content = Unpooled.buffer();
                content.writeShort(2);
                content.writeShort(loginPwd.length);
                content.writeBytes(loginPwd);
                resp = AeroMsgBuilder.buildAckMessage(imei,FunctionType.REGISTER, header.getSerial(),attr,content);
                content.release();
                break;
            case LOGIN:
                Body body = data.getBodies().get(0);
                String loginFactor = body.getLoginPwd();
                String loginPwdCache = redisTemplate.opsForValue().get("Register:" + header.getImei());
                long rebootCount = body.getRebootCount();
                log.info("设备imei：{}已重启{}次",imei, rebootCount);
                if(loginPwdCache!=null && loginPwdCache.equalsIgnoreCase(loginFactor)){
                    log.info("设备imei：{},登录成功！",imei);
                    resp = AeroMsgBuilder.buildResponse(header, StatusCode.SUCCESS);
                }else {
                    log.info("设备imei：{},登录口令错误, src = {}, reality = {}",imei, loginPwdCache, loginFactor);
                    ctx.close();
                }
                break;
            case TIME:
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
                long dutc = data.getBodies().get(0).getDeviceUtc();
                long utc = System.currentTimeMillis();
                long offset = utc - dutc;
                log.info("设备时间：{}", new Date(dutc));
//                if(offset > 10000){
//                    log.warn("设备与平台的时间差较大，下发时间校正指令！");
//                    byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.REFUSE, EnvType.DEBUG,, RequestType.SETTING, DataType.TLV);
//                    ByteBuf content = Unpooled.buffer();
//                    byte[] utcBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
//                    content.writeBytes(BytesUtil.int2TwoBytes(2));
//                    content.writeBytes(BytesUtil.int2TwoBytes(utcBytes.length));
//                    content.writeBytes(utcBytes);
//                    content.capacity(content.readableBytes());
//                    ByteBuf timeAdjustCmd = AeroMsgBuilder.buildMessage(imei,FunctionType.TIME,attr, content);
//                    ctx.writeAndFlush(timeAdjustCmd);
//                }
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

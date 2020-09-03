package com.aero.std.handler;

import com.aero.beans.base.Body;
import com.aero.beans.base.Header;
import com.aero.beans.base.Message;
import com.aero.beans.constants.FormatType;
import com.aero.beans.constants.FunctionType;
import com.aero.beans.constants.RequestType;
import com.aero.beans.constants.StatusCode;
import com.aero.beans.content.DetectData;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.sdk.AeroParser;
import com.aero.std.common.utils.BytesUtil;
import com.aero.std.context.SessionContext;
import com.aero.std.grpc.ReplyUtil;
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
import org.springframework.data.redis.util.ByteUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

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

    @Autowired
    SessionContext sessionContext;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        try {
            String remoteAddr = ctx.channel().remoteAddress().toString();
//            log.info("判断是否阿里云health-check: ip={}", remoteAddr);
            if (remoteAddr.contains("100.")) {
                return;
            }
            log.warn("与设备建立连接，remote:{}", ctx.channel().remoteAddress());
        } finally {
            ctx.fireChannelActive();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String remoteAddr = ctx.channel().remoteAddress().toString();
//        log.info("判断是否阿里云health-check: ip={}", remoteAddr);
        if (remoteAddr.contains("100.")) {
            ctx.close();
            return;
        }
        log.warn("与设备断开连接");
        String imei = sessionContext.getImei(ctx);
        sessionContext.removeChannel(imei);
        ctx.close();
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
        Body body = data.getBody();
        switch (func){
            case HEART_BEAT:
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
                break;
            case REGISTER:
//                long registrerUtc = body.getDeviceUtc();
                long registrerUtc = System.currentTimeMillis();
                String factor = StringUtils.join(imei,registrerUtc);
                String md5 = DigestUtils.md5DigestAsHex(factor.getBytes());
                String pwd = md5.substring(md5.length()-8);
                redisTemplate.opsForValue().set("Register:" + header.getImei(), pwd);
                log.info("设备{}注册，计算并保存登录口令：{}",header.getImei(), pwd);
                byte[] loginPwd = pwd.getBytes();
                byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.SUCCESS, AeroConst.ENV, FormatType.TLV,RequestType.PUBLISH_ACK);
                ByteBuf content = Unpooled.buffer();
                content.writeShort(1);
                content.writeShort(loginPwd.length);
                content.writeBytes(loginPwd);
                resp = AeroMsgBuilder.buildAckMessage(imei,FunctionType.REGISTER, header.getSerial(),attr,content);
                content.release();
                break;
            case LOGIN:
                String loginFactor = body.getLoginPwd();
                String loginPwdCache = redisTemplate.opsForValue().get("Register:" + header.getImei());
                long rebootCount = body.getRebootCount();
                log.info("设备imei：{}已重启{}次",imei, rebootCount);
                if(loginPwdCache!=null && loginPwdCache.equalsIgnoreCase(loginFactor)){
                    log.info("设备imei：{},登录成功！",imei);
                    byte[] loginAttr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.SUCCESS, AeroConst.ENV, FormatType.TLV,RequestType.PUBLISH_ACK);
                    ByteBuf loginAckContent = Unpooled.buffer();
                    loginAckContent.writeShort(3);
                    loginAckContent.writeShort(6);
                    byte[] timeBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
                    loginAckContent.writeBytes(timeBytes);
                    resp = AeroMsgBuilder.buildAckMessage(imei,FunctionType.LOGIN, header.getSerial(),loginAttr,loginAckContent);
//                    resp = AeroMsgBuilder.buildResponse(header, StatusCode.SUCCESS);
                }else {
                    log.info("设备imei：{},登录口令错误, src = {}, reality = {}",imei, loginPwdCache, loginFactor);
                    ctx.close();
                }
                break;
            case TIME:
                log.info("校时请求：imei = {}", imei);
                resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
//                long dutc = body.getDeviceUtc();
//                long utc = System.currentTimeMillis();
//                long offset = utc - dutc;

                break;
            case CORE_DATA:
                log.info("收到核心数据：HEX = {}", BytesUtil.bytes2HexWithBlank(((byte[]) body.getCoreData()),true));

//                DetectData detectData = body.getDetectData();
//                if(Objects.nonNull(detectData)) {
//                    List<Float> currents = detectData.getChannelCurrent();
//                    if (Objects.nonNull(currents)) {
//                        StringBuilder sb = new StringBuilder();
//                        currents.forEach(new Consumer<Float>() {
//                            @Override
//                            public void accept(Float aFloat) {
//                                sb.append(aFloat);
//                                sb.append(",");
//                            }
//                        });
//                        log.info("收到设备采集电流数据(uA)：{}", sb.toString());
//                    }
//                    Integer freq = detectData.getFrequency();
//                    if(Objects.nonNull(freq)){
//                        log.info("收到设备采集频率数据(Hz)：{}", freq);
//                    }
                    resp = AeroMsgBuilder.buildResponse(header, StatusCode.ACCEPT);
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
        //删除过期的key
        sessionContext.replyFutureCache.cleanUp();
        CompletableFuture<Message> replyFuture = ReplyUtil.signMatch(sessionContext.replyFutureCache.asMap(),data);
        if (replyFuture!=null) {
            replyFuture.complete(data);
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
        String remoteAddr = ctx.channel().remoteAddress().toString();
        if (remoteAddr.contains("100.")) {
            ctx.close();
            return;
        }
        log.error("DataActionHandler 发生异常：{}", cause);
        ctx.close();
    }
}

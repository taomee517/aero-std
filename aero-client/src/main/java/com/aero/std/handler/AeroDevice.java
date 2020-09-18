package com.aero.std.handler;

import com.aero.beans.base.Body;
import com.aero.beans.base.Message;
import com.aero.beans.constants.*;
import com.aero.beans.content.DeviceInfo;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.sdk.AeroParser;
import com.aero.std.common.utils.BytesUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗涛
 * @title AeroDevice
 * @date 2020/5/11 18:26
 */
@Slf4j
@ChannelHandler.Sharable
public class AeroDevice extends ChannelDuplexHandler {
    public static InetSocketAddress remoteAddr;
    public static NioEventLoopGroup workers;
    public static Bootstrap client;

    //重连次数
    private int retry = 0;
    private String imei;
    private EnvType envType;
    private static long rebootCount = 0L;
    public static String loginPwd;
    public boolean isLogin = false;
    public static final AttributeKey<ChannelHandler> DEVICE = AttributeKey.valueOf("DEVICE");

    public AeroDevice(String imei, EnvType envType){
        this.imei = imei;
        this.envType = envType;
        workers.schedule(connect, 0, TimeUnit.SECONDS);
    }

    Runnable connect = new Runnable() {
        @Override
        public void run() {
            ChannelFuture future = null;
            synchronized (DEVICE) {
                if (StringUtils.isNotEmpty(loginPwd)) {
                    future =  client.attr(DEVICE, AeroDevice.this)
                            .connect(remoteAddr);
                }else {
                    future =  client.attr(DEVICE, new AeroDevice.RegisterHandler())
                            .connect(remoteAddr);
                }
            }

            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(!future.isSuccess()){
                        reconnect();
                    }
                }
            });
        }
    };

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        retry = 0;
        ByteBuf loginMsg = buildLogin(imei,AeroConst.ENV,loginPwd, rebootCount);
        String loginHex = AeroParser.buffer2Hex(loginMsg);
        log.info("登录消息, hex: {}", loginHex);
        ctx.writeAndFlush(loginMsg);

        //发送登录或状态消息
//        ByteBuf loginMsg = buildLogin(imei,envType);
//        String loginHex = AeroParser.buffer2Hex(loginMsg);
//        log.info("登录消息, hex: {}", loginHex);
//        ctx.writeAndFlush(loginMsg);

//        ByteBuf timeMsg = buildTimeReport(imei,envType);
//        String timeHex = AeroParser.buffer2Hex(timeMsg);
//        log.info("时间消息, hex: {}", timeHex);
//        ctx.writeAndFlush(timeMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("设备{}与平台断开连接！", this.imei);
        isLogin = false;
        ctx.close();
        reconnect();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Message m = ((Message) msg);
        String hexBuf = AeroParser.buffer2Hex(m.getHeader().getRaw());
        log.info("设备{}收到服务器消息：type = {},msg = {}", this.imei, m.getHeader().getFun(), hexBuf);
        FunctionType fun = m.getHeader().getFun();
        RequestType request = m.getHeader().getRequest();
        if(FunctionType.REGISTER.equals(fun)){
            loginPwd = m.getBody().getLoginPwd();
            log.info("设备登录口令：{}",loginPwd);
            ByteBuf loginMsg = buildLogin(imei,AeroConst.ENV,loginPwd, rebootCount);
            String loginHex = AeroParser.buffer2Hex(loginMsg);
            log.info("登录消息, hex: {}", loginHex);
            ctx.writeAndFlush(loginMsg);
        }else if(FunctionType.REBOOT.equals(fun)){
            rebootCount++;
            log.info("收到设备重启指令, hex: {}", hexBuf);
            RequestType ackType = RequestType.getRequestType(m.getHeader().getRequest().getAckCode());
            byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION,StatusCode.ACCEPT,AeroConst.ENV, FormatType.TLV,ackType);
            ByteBuf rebootAck = AeroMsgBuilder.buildAckMessage(imei,m.getHeader().getFun(),m.getHeader().getSerial(),attr,null);
            ctx.writeAndFlush(rebootAck);
            reconnect();
        }else if (FunctionType.LOGIN.equals(fun)){
            StatusCode statusCode = m.getHeader().getStatusCode();
            log.info("登录回复, statusCode = {}, timestamp = {}", statusCode, m.getBody().getServerUtc());
            if(StatusCode.SUCCESS.equals(statusCode)){
                isLogin = true;
            }
        }

        switch (request){
            case QUERY:
                if(FunctionType.DEVICE_INFO.equals(fun)){
                    RequestType ackType = RequestType.getRequestType(m.getHeader().getRequest().getAckCode());
                    byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION,StatusCode.SUCCESS,AeroConst.ENV, FormatType.TLV,ackType);
                    ByteBuf content = Unpooled.buffer();
                    content.writeShort(1);
                    content.writeShort(imei.length());
                    content.writeBytes(imei.getBytes());
                    ByteBuf rebootAck = AeroMsgBuilder.buildAckMessage(imei,m.getHeader().getFun(),m.getHeader().getSerial(),attr,content);
                    ctx.writeAndFlush(rebootAck);
                    content.release();
                }
                break;
            case SETTING:
                Body body = m.getBody();
                if(Objects.isNull(body)){
                    log.error("解析结果BODY为空！！");
                    return;
                }
                DeviceInfo deviceInfo = body.getDeviceInfo();
                switch (fun){
                    case SERVER_URL:
                        log.info("收到设置上线地址指令：master = {}, slave = {}", deviceInfo.getMasterUrl(), deviceInfo.getSlaveUrl());
                        break;
                    case INTERVAL:
                        log.info("收到设置频率指令：heart = {}, collect = {}", deviceInfo.getHeartBeatInterval(), deviceInfo.getDetectInterval());
                        break;
                    case LOWER_THRESHOLD:
                        break;
                }
                RequestType ackType = RequestType.getRequestType(m.getHeader().getRequest().getAckCode());
                byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION,StatusCode.SUCCESS,AeroConst.ENV, FormatType.TLV,ackType);
                ByteBuf settingAck = AeroMsgBuilder.buildAckMessage(imei,m.getHeader().getFun(),m.getHeader().getSerial(),attr,null);
                ctx.writeAndFlush(settingAck);
                break;
            case EXECUTE:
            case PUBLISH:
                break;
        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
//        log.info("执行flush");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleEvt = ((IdleStateEvent) evt);
            if(idleEvt.state().equals(IdleState.READER_IDLE)){
//                sendHeartbeat(ctx);
                boolean flag = new Random().nextBoolean();
                if (flag) {
                    sendDetectCurrentData(ctx);
                }else {
                    sendDetectFrequencyData(ctx);
                }

//                String hex = "7e 08 13 46 33 46 54 13 68 01 00 84 20 00 00 0f 00 00 00 08 00 02 00 04 00 00 01 7d 01 be 7e";
//                byte[] bytes = BytesUtil.hex2Bytes(hex);
//                ByteBuf buf = Unpooled.wrappedBuffer(bytes);
//                ctx.writeAndFlush(buf);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：" + cause.getMessage(), cause);
        ctx.close();
    }

    private int getReconnectInterval(){
        return Math.min(15, retry);
    }

    private void reconnect(){
        if (!isLogin) {
            retry ++;
            int interval = getReconnectInterval();
            log.info("{}分钟后发起重连！",interval);
            //重连间隔暂时写死
            workers.schedule(connect, interval, TimeUnit.MINUTES);
        }
    }

    private void sendHeartbeat(ChannelHandlerContext ctx){
        ByteBuf hb = buildHeartBeat(imei,envType);
        String hexMsg = AeroParser.buffer2Hex(hb);
        log.info("发送心跳, hex: {}", hexMsg);

//        String src = "7e 08 13 46 33 46 54 13 68 00 7d 00 7e 00 03 00 10 00 00 49 33 09 42 7e";
//        src = StringUtils.replace(src," ", "");
//        byte[] bytes = BytesUtil.hex2Bytes(src);
//        ByteBuf msg = Unpooled.wrappedBuffer(bytes);
        ByteBuf buffer = AeroParser.escape(hb);
        ctx.channel().writeAndFlush(buffer);
    }

    private void sendDetectCurrentData(ChannelHandlerContext ctx){
        ByteBuf coreBuffer = Unpooled.buffer(16+4);
        //与硬件保持一致
        coreBuffer.writeShort(3);
        coreBuffer.writeShort(4*4);
        for(int i=0;i<4;i++){
            float singleData = new Random().nextFloat() * 2000 + 2000;
            coreBuffer.writeFloat(singleData);
        }
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION,StatusCode.NONE,AeroConst.ENV,FormatType.TLV,RequestType.PUBLISH);
        ByteBuf data = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.CORE_DATA,attr,coreBuffer);
//        String hexMsg = AeroParser.buffer2Hex(data);
//        log.info("发送采集电流数据, hex: {}", hexMsg);
        ctx.writeAndFlush(data);
    }

    private void sendDetectFrequencyData(ChannelHandlerContext ctx){
        ByteBuf coreBuffer = Unpooled.buffer(4+4);
        coreBuffer.writeShort(2);
        coreBuffer.writeShort(4);
        coreBuffer.writeInt(381);
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION,StatusCode.NONE,AeroConst.ENV,FormatType.TLV,RequestType.PUBLISH);
        ByteBuf data = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.CORE_DATA,attr,coreBuffer);
//        String hexMsg = AeroParser.buffer2Hex(data);
//        log.info("发送采集频率数据, hex: {}", hexMsg);
        ctx.writeAndFlush(data);
    }

    private ByteBuf buildHeartBeat(String imei, EnvType env){
        byte[] attr = buildRequestAtrribute(env);
        ByteBuf msg = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.HEART_BEAT, attr,null);
        return msg;
    }

    private ByteBuf buildLogin(String imei, EnvType env, String loginPwd, long rebootCount){
        byte[] attr = buildRequestAtrribute(env);
        ByteBuf content = Unpooled.buffer();
        byte[] loginBytes = loginPwd.getBytes();
        content.writeShort(1);
        content.writeShort(loginBytes.length);
        content.writeBytes(loginBytes);
        content.writeShort(2);
        content.writeShort(8);
        content.writeLong(rebootCount);
        ByteBuf msg = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.LOGIN, attr,content);
        content.release();
        return msg;
    }

    private ByteBuf buildRegister(String imei, EnvType env){
        byte[] attr = buildRequestAtrribute(env);
//        ByteBuf content = Unpooled.buffer();
//        byte[] utcBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
//        content.writeShort(1);
//        content.writeShort(utcBytes.length);
//        content.writeBytes(utcBytes);
        ByteBuf msg = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.REGISTER, attr,null);
        return msg;
    }

    private ByteBuf buildTimeReport(String imei, EnvType env){
        byte[] attr = buildRequestAtrribute(env);
        ByteBuf content = Unpooled.buffer();
        byte[] utcBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
        content.writeShort(1);
        content.writeShort(utcBytes.length);
        content.writeBytes(utcBytes);
        ByteBuf msg = AeroMsgBuilder.buildRequestMessage(imei,FunctionType.TIME, attr,content);
        content.release();
        return msg;
    }

    private byte[] buildRequestAtrribute(EnvType env){
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.NONE,
                env, FormatType.TLV,RequestType.PUBLISH);
        return attr;
    }


    public class RegisterHandler extends ChannelInboundHandlerAdapter{
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.warn("设备{}与平台建立连接！", AeroDevice.this.imei);
            ByteBuf registerMsg = buildRegister(imei, envType);
            String registerHex = AeroParser.buffer2Hex(registerMsg);
            log.info("注册消息, hex: {}", registerHex);
            ctx.writeAndFlush(registerMsg);
            ctx.channel().pipeline().replace(this, "device", AeroDevice.this);
//            ctx.channel().pipeline().remove(this);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("设备{}注册失败，与平台断连！", AeroDevice.this.imei);
            ctx.close();
            reconnect();
        }
    }

}

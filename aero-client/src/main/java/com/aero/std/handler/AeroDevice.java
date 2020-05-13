package com.aero.std.handler;

import com.aero.beans.constants.*;
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

import java.net.InetSocketAddress;
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


    public static final AttributeKey<ChannelHandler> DEVICE = AttributeKey.valueOf("DEVICE");
    public AeroDevice(String imei){
        this.imei = imei;
        workers.schedule(connect, 0, TimeUnit.SECONDS);
    }

    Runnable connect = new Runnable() {
        @Override
        public void run() {
            ChannelFuture future = null;
            synchronized (DEVICE) {
                future =  client.attr(DEVICE, AeroDevice.this)
                        .connect(remoteAddr);
            }

            future.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(!future.isSuccess()){
                        log.info("1分钟后发起重连！");
                        //重连间隔暂时写死
                        workers.schedule(connect, 1, TimeUnit.MINUTES);
                        retry ++;
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
        log.warn("设备{}与平台建立连接！", this.imei);
        //发送登录或状态消息
        ByteBuf loginMsg = buildLogin(imei,EnvType.DEBUG);
        String loginHex = AeroParser.buffer2Hex(loginMsg);
        log.info("登录消息, hex: {}", loginHex);
        ctx.writeAndFlush(loginMsg);

        ByteBuf timeMsg = buildTimeReport(imei,EnvType.DEBUG);
        String timeHex = AeroParser.buffer2Hex(timeMsg);
        log.info("时间消息, hex: {}", timeHex);
        ctx.writeAndFlush(timeMsg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("设备{}与平台断开连接！", this.imei);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        String hexBuf = AeroParser.buffer2Hex(buf);
        log.info("设备{}收到服务器消息：{}", this.imei, hexBuf);
        if(hexBuf.contains("00 02")){

        }
    }

    @Override
    public void flush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
        log.info("执行flush");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent idleEvt = ((IdleStateEvent) evt);
            if(idleEvt.state().equals(IdleState.READER_IDLE)){
                sendHeartbeat(ctx);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("发生异常：{}", cause);
        ctx.close();
    }

    private void sendHeartbeat(ChannelHandlerContext ctx){
        ByteBuf hb = buildHeartBeat(imei,EnvType.DEBUG);
        String hexMsg = AeroParser.buffer2Hex(hb);
        log.info("发送心跳, hex: {}", hexMsg);

//        String src = "7e 08 13 46 33 46 54 13 68 00 7d 00 7e 00 03 00 10 00 00 49 33 09 42 7e";
//        src = StringUtils.replace(src," ", "");
//        byte[] bytes = BytesUtil.hex2Bytes(src);
//        ByteBuf msg = Unpooled.wrappedBuffer(bytes);
        ByteBuf buffer = AeroParser.escape(hb);
        ctx.channel().writeAndFlush(buffer);
    }

    private ByteBuf buildHeartBeat(String imei, EnvType env){
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.REQUEST,RequestType.PUBLISH,DataType.TLV,
                env, false, EncryptType.CRC, ValidateType.CRC);
        ByteBuf msg = AeroMsgBuilder.buildMessage(imei,FunctionType.HEART_BEAT, attr,null);
        return msg;
    }

    private ByteBuf buildLogin(String imei, EnvType env){
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.REQUEST,RequestType.PUBLISH,DataType.TLV,
                env, false, EncryptType.CRC, ValidateType.CRC);
        ByteBuf msg = AeroMsgBuilder.buildMessage(imei,FunctionType.LOGIN, attr,null);
        return msg;
    }

    private ByteBuf buildTimeReport(String imei, EnvType env){
        byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.REQUEST,RequestType.PUBLISH,DataType.TLV,
                env, false, EncryptType.CRC, ValidateType.CRC);
        ByteBuf content = Unpooled.buffer();
        byte[] utcBytes = BytesUtil.utc2Bytes(System.currentTimeMillis());
        content.writeBytes(BytesUtil.int2TwoBytes(2));
        content.writeBytes(BytesUtil.int2TwoBytes(utcBytes.length));
        content.writeBytes(utcBytes);
        ByteBuf msg = AeroMsgBuilder.buildMessage(imei,FunctionType.TIME, attr,content);
        return msg;
    }
}

package com.aero.std.handler;

import com.aero.std.common.constants.*;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.utils.SnUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
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
//        sendLog(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn("设备{}与平台断开连接！", this.imei);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("设备{}收到服务器消息：{}", this.imei, msg.toString());
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
        byte[] attr = AeroMsgBuilder.buildAttribute(DataType.TCP, EnvType.DEBUG, EncryptType.CRC, ValidateType.CRC, RequestType.PUBLISH,false,"1.0",0,0);
        int serial = SnUtil.getSn();
        ByteBuf msg = AeroMsgBuilder.buildMessage(imei,serial, 0, attr,null);
        log.info("发送心跳, serial: {}", serial);
        ctx.writeAndFlush(msg);
    }
}

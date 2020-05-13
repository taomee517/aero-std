package com.aero.std.handler;

import com.aero.beans.base.Message;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
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
        ByteBuf resp = AeroMsgBuilder.buildResponse(data.getHeader());
        String hexResp = AeroParser.buffer2Hex(resp);
        log.info("响应设备：{}", hexResp);
        ctx.writeAndFlush(resp);
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

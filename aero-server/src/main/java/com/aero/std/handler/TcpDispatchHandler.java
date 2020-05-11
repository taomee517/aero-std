package com.aero.std.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title TcpDispatchHandler
 * @date 2020/5/8 12:17
 */
@ChannelHandler.Sharable
@Component
@Slf4j
public class TcpDispatchHandler extends ChannelInboundHandlerAdapter {
//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        String address = ctx.channel().localAddress().toString();
//        int port = Integer.valueOf(StringUtils.split(address, ":")[1]);
//        log.info("请求端口：" + port);
//    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String address = ctx.channel().localAddress().toString();
        int port = Integer.valueOf(StringUtils.split(address, ":")[1]);
        log.info("请求端口：" + port);
        this.handlerRemoved(ctx);
        ctx.fireChannelActive();
    }
}

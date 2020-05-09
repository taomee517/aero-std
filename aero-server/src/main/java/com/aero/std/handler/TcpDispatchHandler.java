package com.aero.std.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title TcpDispatchHandler
 * @date 2020/5/8 12:17
 */
@Component
public class TcpDispatchHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String address = ctx.channel().localAddress().toString();
        int port = Integer.valueOf(StringUtils.split(address, ":")[1]);
        System.out.println("请求端口：" + port);
    }
}

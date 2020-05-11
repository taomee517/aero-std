package com.aero.std.handler;

import com.aero.std.common.utils.BytesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title ContentValidateHandler
 * @date 2020/5/8 11:30
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class ContentValidateHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        log.info("收到设备消息：{}", BytesUtil.bytes2HexWithBlank(bytes,true));
        super.channelRead(ctx, msg);
    }
}

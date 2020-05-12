package com.aero.std.handler;

import com.aero.std.common.sdk.AeroParser;
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
        if(AeroParser.validate(buf)){
            log.error("消息校验通过");
            ctx.fireChannelRead(ctx);
        }else {
            String hexMsg = AeroParser.buffer2Hex(buf);
            log.error("消息校验失败：{}", hexMsg);
        }
    }
}
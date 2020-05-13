package com.aero.std.handler;

import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title UnescapeHandler
 * @date 2020/5/8 11:23
 */
@Component
@ChannelHandler.Sharable
public class EscapeHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        buf = AeroParser.escape(buf);
//        ctx.writeAndFlush(buf);
        super.write(ctx,buf,promise);
    }
}

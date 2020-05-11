package com.aero.std.handler;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 罗涛
 * @title HeaderParseHandler
 * @date 2020/5/8 11:31
 */
@Component
@ChannelHandler.Sharable
public class HeaderParseHandler extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {

    }
}

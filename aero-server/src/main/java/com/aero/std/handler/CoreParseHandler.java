package com.aero.std.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 罗涛
 * @title CoreParseHandler
 * @date 2020/5/8 12:16
 */
@Component
public class CoreParseHandler extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {

    }
}

package com.aero.std.handler;

import com.aero.beans.base.Header;
import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 罗涛
 * @title HeaderParseHandler
 * @date 2020/5/8 11:31
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HeaderParseHandler extends MessageToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        Header header = AeroParser.parseHeader(buf);
        log.info("解析出消息头：{}",header);
    }
}

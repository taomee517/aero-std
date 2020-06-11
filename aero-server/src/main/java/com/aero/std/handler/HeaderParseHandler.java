package com.aero.std.handler;

import com.aero.beans.base.Header;
import com.aero.std.common.sdk.AeroParser;
import com.aero.std.context.SessionContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    SessionContext sessionContext;

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        ByteBuf buf = ((ByteBuf) msg);
        Header header = AeroParser.parseHeader(buf);
        sessionContext.putChannel(header.getImei(),ctx.channel());
        sessionContext.putImei(ctx, header.getImei());
        log.info("解析出消息头：{}",header);
        out.add(header);
    }
}

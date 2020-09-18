package com.aero.std.handler;

import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title UnescapeHandler
 * @date 2020/5/8 11:23
 */
@Component
@ChannelHandler.Sharable
@Slf4j
public class EscapeHandler extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
//        byte[] bytes = ((byte[]) msg);
//        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);
        ByteBuf buffer = (ByteBuf) msg;
//        showHexMsg(buffer,true);
        ByteBuf escapeBuffer = AeroParser.escape(buffer);
        showHexMsg(escapeBuffer,false);
        out.writeBytes(escapeBuffer);
    }

    private void showHexMsg(ByteBuf buf, boolean before){
        String escapeMsg = AeroParser.buffer2Hex(buf);
        String beforeChar = before?"前":"后";
        log.info("转义{}的消息为：{}", beforeChar, escapeMsg);
    }
}

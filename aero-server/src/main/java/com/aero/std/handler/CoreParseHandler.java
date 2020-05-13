package com.aero.std.handler;

import com.aero.beans.base.Body;
import com.aero.beans.base.Header;
import com.aero.beans.base.Message;
import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
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
@ChannelHandler.Sharable
public class CoreParseHandler extends MessageToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List out) throws Exception {
        Header header = ((Header) msg);
        ByteBuf content = header.getContent();
        List<Body> bodies = AeroParser.parseBody(content);
        Message message = new Message();
        message.setHeader(header);
        message.setBodies(bodies);
        out.add(message);
    }
}

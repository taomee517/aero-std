package com.aero.std.handler;

import com.aero.beans.constants.FormatType;
import com.aero.beans.constants.StatusCode;
import com.aero.std.common.constants.AeroConst;
import com.aero.std.common.sdk.AeroMsgBuilder;
import com.aero.std.grpc.Order;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title OrderConvertHandler
 * @date 2020/6/11 14:16
 */
@Component
@ChannelHandler.Sharable
public class OrderConvertHandler extends ChannelOutboundHandlerAdapter {
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(msg instanceof Order){
            Order order = ((Order) msg);
            byte[] attr = AeroMsgBuilder.buildAttribute(AeroConst.PROTOCOL_VERSION, StatusCode.NONE,AeroConst.ENV, FormatType.TLV, order.getRequestType());
            ByteBuf content = order.getParams()==null?null:Unpooled.wrappedBuffer(order.getParams());
            ByteBuf buffer = AeroMsgBuilder.buildMessage(order.getImei(),order.getOrderId(),0, order.getFunctionType().getCode(),attr, content);
            ctx.writeAndFlush(buffer);
        }else {
            ctx.writeAndFlush(msg);
        }
    }
}

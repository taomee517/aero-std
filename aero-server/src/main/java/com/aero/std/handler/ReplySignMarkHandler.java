package com.aero.std.handler;

import com.aero.std.context.SessionContext;
import com.aero.std.grpc.DownStreamMsg;
import com.aero.std.grpc.ReplySign;
import com.aero.std.grpc.ReplyUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title ReplySignMarkHandler
 * @date 2020/6/11 10:53
 */
@Component
@Slf4j
@ChannelHandler.Sharable
public class ReplySignMarkHandler extends ChannelOutboundHandlerAdapter {
    @Autowired
    SessionContext sessionContext;

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if(!(msg instanceof DownStreamMsg)){
            log.error("下行指令封装错误！msg = {}",msg);
            return;
        }
        DownStreamMsg downStreamMsg = ((DownStreamMsg) msg);
        //创建并记录回复标志，用于后续上行消息的过滤
        ReplySign replySign = ReplyUtil.buildReplySign(downStreamMsg.getOrder());
        sessionContext.putReplyFuture(replySign,downStreamMsg.getMonitorFuture());
        ctx.writeAndFlush(downStreamMsg.getOrder());
    }
}

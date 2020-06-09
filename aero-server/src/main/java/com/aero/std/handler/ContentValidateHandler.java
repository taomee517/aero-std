package com.aero.std.handler;

import com.aero.beans.constants.EnvType;
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
        if(EnvType.DEBUG.equals(getEnvType(buf))){
            log.error("开发环境，无须校验");
            ctx.fireChannelRead(buf);
        }else if(AeroParser.validate(buf)){
            log.info("消息校验通过");
            ctx.fireChannelRead(buf);
        }else {
            String hexMsg = AeroParser.buffer2Hex(buf);
            log.error("消息校验失败：{}", hexMsg);
        }
    }

    private EnvType getEnvType(ByteBuf buf){
        byte[] srcAttr = new byte[4];
        buf.getBytes(9,srcAttr);
        int attr = BytesUtil.bytes2Int(srcAttr);
        int envCode = attr >> 7 & 1;
        EnvType envType = EnvType.getEnvType(envCode);
        return envType;
    }
}

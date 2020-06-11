package com.aero.std.handler;

import com.aero.std.common.sdk.AeroParser;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author 罗涛
 * @title FrameSplitHandler
 * @date 2020/5/8 11:19
 */
@Component
@Slf4j
public class FrameSplitHandler extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            ByteBuf frame = AeroParser.split(in);
            showHexData(frame);
            out.add(frame);
        } finally {
            resetBuffer(in);
        }
    }

    private void resetBuffer(ByteBuf buffer){
        int left = buffer.readableBytes();
        int start = buffer.readerIndex();
        if (left == 0 && buffer.readerIndex() > 0){
            buffer.setIndex(0, 0);
            return;
        }
        if (left > 0 && buffer.readerIndex() > 0) {
            for (int index = 0; index < left; index++)
                buffer.setByte(index, buffer.getByte(index + start));
        }
        buffer.setIndex(0, left);
    }


    private void showHexData(ByteBuf frame){
        String hexMsg = AeroParser.buffer2Hex(frame);
        log.info("收到Server端消息：{}", hexMsg);
    }
}

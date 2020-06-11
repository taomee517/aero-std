package com.aero.std.server;

import com.aero.std.context.ProtocolContext;
import com.aero.std.handler.FrameSplitHandler;
import com.aero.std.handler.HandlerCenter;
import com.aero.std.protocol.IProtocol;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class NettyServer implements SmartLifecycle {

    @Autowired
    HandlerCenter handlers;

    NioEventLoopGroup boss;
    NioEventLoopGroup workers;
    Channel channel = null;

    Map<Integer, Channel> channelMap = new ConcurrentHashMap<>();

    boolean running;

    @Override
    public void start() {
        try {
            ServerBootstrap server = new ServerBootstrap();
            boss = new NioEventLoopGroup();
            workers = new NioEventLoopGroup(8);

            server.group(boss, workers)
                    .option(ChannelOption.SO_RCVBUF, 1024*10)
                    .option(ChannelOption.ALLOCATOR, UnpooledByteBufAllocator.DEFAULT)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            pipeline.addLast("tcp-dispatch", handlers.tcpDispatchHandler);
                            pipeline.addLast("encoder", handlers.frameEncoder);
                            //效果FrameSplitHandler类似，但传给后续handler的内容去掉了首尾
//                            pipeline.addLast("decoder", new DelimiterBasedFrameDecoder(AeroConst.MAX_LENGTH, Unpooled.buffer(1).writeByte(AeroConst.SIGN_CODE)));
                            pipeline.addLast("split", new FrameSplitHandler());
                            pipeline.addLast("unescape", handlers.unescapeHandler);
                            pipeline.addLast("escape", handlers.escapeHandler);
                            pipeline.addLast("validator", handlers.contentValidateHandler);
                            pipeline.addLast("head", handlers.headerParseHandler);
                            pipeline.addLast("core", handlers.coreParseHandler);
                            pipeline.addLast("action", handlers.dataActionHandler);
                            pipeline.addLast("order-convert", handlers.orderConvertHandler);
                            pipeline.addLast("sign-mark", handlers.replySignMarkHandler);
                        }
                    });
            for(IProtocol tcp: ProtocolContext.getAllProtocols()){
                ChannelFuture channelFuture = server.bind(tcp.port()).sync();
                channel = channelFuture.channel();
                channelMap.put(tcp.port(), channel);
            }
            running = true;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        try {
            for(Channel channel : channelMap.values()){
                channel.close().syncUninterruptibly();
            }
            boss.shutdownGracefully();
            workers.shutdownGracefully();
            boss.awaitTermination(30, TimeUnit.SECONDS);
            workers.awaitTermination(30,TimeUnit.SECONDS);
            running = false;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public boolean isRunning() {
        return false;
    }
}

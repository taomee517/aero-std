package com.aero.std.client;

import com.aero.std.handler.AeroDevice;
import com.aero.std.handler.EscapeHandler;
import com.aero.std.handler.FrameSplitHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class Client implements InitializingBean, DisposableBean {

    @Value("${remote.server.ip}")
    String ip;

    @Value("${remote.server.port}")
    int port;

    Bootstrap client = new Bootstrap();
    NioEventLoopGroup workers = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2);
    InetSocketAddress remoteAddr;

    @Override
    public void destroy() throws Exception {

    }

    @Override
    public void afterPropertiesSet() throws Exception {
        remoteAddr = new InetSocketAddress(ip,port);
        client.group(workers)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
//                        pipeline.addLast("encoder", new Encoder());
                        pipeline.addLast("split", new FrameSplitHandler());
//                        pipeline.addLast("validator", new Validator());
//                        pipeline.addLast("head", new HeaderParseHandler());
//                        pipeline.addLast("escape", new EscapeHandler());
                        pipeline.addLast("idle", new IdleStateHandler(20000,0,0, TimeUnit.MILLISECONDS));
                        pipeline.addLast("device", ch.attr(AeroDevice.DEVICE).get());
                    }
                });
        AeroDevice.client = client;
        AeroDevice.workers = workers;
        AeroDevice.remoteAddr = remoteAddr;
    }
}

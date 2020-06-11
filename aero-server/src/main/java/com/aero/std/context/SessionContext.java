package com.aero.std.context;

import com.aero.std.grpc.ReplySign;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 罗涛
 * @title SessionContext
 * @date 2020/6/11 9:57
 */
@Component
@Slf4j
public class SessionContext {

    private AttributeKey<String> IMEI_KEY = AttributeKey.newInstance("imei");

    // 会话map imei -> channel
    public Cache<String, Channel> channelCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES).build();

    //结果标志 ->  promise
    public Cache<ReplySign, CompletableFuture> replyFutureCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.SECONDS)
            .removalListener(new RemovalListener<ReplySign, CompletableFuture>() {
                @Override
                public void onRemoval(RemovalNotification<ReplySign, CompletableFuture> notification) {
                    try {
                        notification.getValue().completeExceptionally(new Throwable("监听超时"));
                    } catch (Exception e) {
                        log.info("监听超时,resultSign:{}", notification.getKey());
                    }
                }
            })
            .build();

    public void putChannel(String imei, Channel channel){
        channelCache.put(imei,channel);
    }

    public Channel getChannel(String imei){
        return channelCache.asMap().get(imei);
    }

    public void removeChannel(String imei){
        channelCache.asMap().remove(imei);
    }

    public void putReplyFuture(ReplySign sign, CompletableFuture future){
        replyFutureCache.put(sign,future);
    }

    public CompletableFuture getReplyFuture(ReplySign sign){
        return replyFutureCache.asMap().get(sign);
    }

    public void removeReplyFuture(ReplySign sign){
        replyFutureCache.asMap().remove(sign);
    }

    public void putImei(ChannelHandlerContext ctx, String imei){
        ctx.channel().attr(IMEI_KEY).set(imei);
    }

    public String getImei(ChannelHandlerContext ctx){
        return ctx.channel().attr(IMEI_KEY).get();
    }
}

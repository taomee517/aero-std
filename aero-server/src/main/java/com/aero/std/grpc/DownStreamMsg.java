package com.aero.std.grpc;

import com.aero.beans.base.Message;
import lombok.Data;

import java.util.concurrent.CompletableFuture;

/**
 * @author 罗涛
 * @title DownStreamMsg
 * @date 2020/6/11 10:32
 */
@Data
public class DownStreamMsg {
    private Order order;
    private CompletableFuture<Message> monitorFuture;
}

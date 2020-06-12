package com.aero.std.grpc;

import com.aero.beans.base.Message;
import com.aero.beans.constants.FunctionType;
import com.aero.beans.constants.RequestType;
import com.aero.std.api.AeroCommand;
import com.aero.std.api.CmdServiceGrpc;
import com.aero.std.common.utils.SnUtil;
import com.aero.std.context.SessionContext;
import io.grpc.stub.StreamObserver;
import io.netty.channel.Channel;
import io.netty.channel.DefaultChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * @author 罗涛
 * @title CtrlService
 * @date 2020/6/10 17:23
 */
@GrpcService
@Slf4j
public class CtrlService extends CmdServiceGrpc.CmdServiceImplBase {
    @Autowired
    SessionContext sessionContext;

    @Override
    public void sendExecuteCmd(AeroCommand.CommandRequest request, StreamObserver<AeroCommand.ExecuteResponse> responseObserver) {
        AeroCommand.CmdEntity cmdEntity = request.getEntity();
        String imei = cmdEntity.getImei();
        int requestCode = cmdEntity.getRequestCode();
        int functionCode = cmdEntity.getFunctionCode();
        Channel channel = sessionContext.getChannel(imei);
        if(Objects.isNull(channel)){
            AeroCommand.ExecuteResponse response = buildResponse(AeroCommand.StatusCode.OFFLINE,"设备不在线", false);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        if(!channel.isActive()){
            AeroCommand.ExecuteResponse response = buildResponse(AeroCommand.StatusCode.CHANNEL_NOT_ACTIVE,"设备与平台连接已失效", false);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            return;
        }
        Order order = new Order();
        order.setOrderId(SnUtil.getSn(imei));
        order.setImei(imei);
        order.setRequestType(RequestType.getRequestType(requestCode));
        order.setFunctionType(FunctionType.getFunctionType(functionCode));
        CompletableFuture<Message> monitorFuture = new CompletableFuture<>();

        monitorFuture.thenAccept(new Consumer<Message>() {
            @Override
            public void accept(Message message) {
                if (Objects.nonNull(message)) {
                    AeroCommand.ExecuteResponse response = buildResponse(AeroCommand.StatusCode.SUCCESS,"执行成功", true);
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
        }).handle((aVoid, throwable) -> {
            if (throwable instanceof CompletionException) {
                AeroCommand.ExecuteResponse response = buildResponse(AeroCommand.StatusCode.EXCEPTION,throwable.getCause().getMessage(), false);
                responseObserver.onNext(response);
            }else {
                responseObserver.onError(throwable);
            }
            responseObserver.onCompleted();
            return null;
        });
        DownStreamMsg downStreamMsg = new DownStreamMsg();
        downStreamMsg.setOrder(order);
        downStreamMsg.setMonitorFuture(monitorFuture);

        DefaultChannelPromise promise = new DefaultChannelPromise(channel);
        promise.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()){
                    log.info("指令发送成功");
                }else {
                    AeroCommand.ExecuteResponse response = buildResponse(AeroCommand.StatusCode.FAIL,"指令发送失败", false);
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
        });
        channel.writeAndFlush(downStreamMsg,promise);
    }


    private AeroCommand.ExecuteResponse buildResponse(AeroCommand.StatusCode statusCode, String msg, Object result){
        AeroCommand.Status status = AeroCommand.Status
                .newBuilder()
                .setStatusCode(statusCode)
                .setMessage(msg)
                .build();
        AeroCommand.ExecuteResponse response = AeroCommand.ExecuteResponse
                .newBuilder()
                .setStatus(status)
                .setResult((boolean)result)
                .build();
        return response;
    }
}

package com.aero.std.grpc;

import com.aero.sdk.basic.order.CtrlServiceGrpc;
import com.aero.std.api.CmdServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * @author 罗涛
 * @title GrpcClient
 * @date 2020/6/11 15:30
 */
public class GrpcClientUtil {

    public static final int GRPC_PORT = 50051;

    public static CmdServiceGrpc.CmdServiceFutureStub getServiceFutureStub(String host){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host,GRPC_PORT)
                .usePlaintext(true)
                .build();
        return CmdServiceGrpc.newFutureStub(channel);
    }

    public static CtrlServiceGrpc.CtrlServiceFutureStub getMixFutureStub(String host){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host,GRPC_PORT)
                .usePlaintext(true)
                .build();
        return CtrlServiceGrpc.newFutureStub(channel);
    }
}

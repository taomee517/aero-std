package com.aero.std.controller;

import com.aero.std.api.AeroCommand;
import com.aero.std.api.CmdServiceGrpc;
import com.aero.std.beans.CmdDTO;
import com.aero.std.grpc.GrpcClientUtil;
import com.google.common.util.concurrent.ListenableFuture;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author 罗涛
 * @title IndexController
 * @date 2020/6/10 09:37
 */
@Slf4j
@RestController
@RequestMapping(value = "/cmd")
@Api(tags = "远程指令接口", value = "CommandController")
public class CommandController {

//    @GrpcClient("CtrlService")
//    private CmdServiceGrpc.CmdServiceFutureStub cmdServiceFutureStub;


    @PostMapping("reboot")
    @ApiOperation("重启")
    public Boolean reboot(@RequestBody CmdDTO dto){
        try {
            AeroCommand.CmdEntity cmdEntity = AeroCommand.CmdEntity.newBuilder()
                    .setImei(dto.getImei())
                    .setRequestCode(dto.getRequestType())
                    .setFunctionCode(dto.getFunctionType())
                    .build();
            AeroCommand.CommandRequest request = AeroCommand.CommandRequest.newBuilder().setEntity(cmdEntity).build();
            CmdServiceGrpc.CmdServiceFutureStub cmdServiceFutureStub = GrpcClientUtil.getServiceFutureStub("localhost");
            ListenableFuture<AeroCommand.ExecuteResponse> respFuture = cmdServiceFutureStub.sendExecuteCmd(request);
//        respFuture.addListener(new Runnable() {
//            @Override
//            public void run() {
//                try {
//                    AeroCommand.ExecuteResponse resp = respFuture.get();
//                    re
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }, Executors.newSingleThreadExecutor());
            AeroCommand.ExecuteResponse resp = respFuture.get(10, TimeUnit.SECONDS);
            AeroCommand.Status status = resp.getStatus();
            log.info("控制结果：statusCode = {}, msg = {}, result = {}", status.getStatusCode(), status.getMessage(),resp.getResult());
            return resp.getResult();
        }catch (TimeoutException ex){
            log.info("控制结果： msg = {}, result = {}", "超时（grpc调用端）", false);
            return false;
        }catch (ExecutionException e) {
            log.info("控制结果： msg = {}, result = {}", e.getCause(), false);
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}

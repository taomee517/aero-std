package com.aero.std.controller;

import com.aero.sdk.basic.order.ControlApi;
import com.aero.sdk.basic.order.CtrlServiceGrpc;
import com.aero.sdk.measure.constants.BusinessType;
import com.aero.sdk.measure.constants.FunctionType;
import com.aero.sdk.measure.constants.HardwareType;
import com.aero.std.grpc.GrpcClientUtil;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
@RequestMapping(value = "/mixCmd")
@Api(tags = "通用指令接口", value = "MixCmdController")
public class MixCmdController {

//    @GrpcClient("CtrlService")
//    private CmdServiceGrpc.CmdServiceFutureStub cmdServiceFutureStub;


    @PostMapping("control")
    @ApiOperation("控制")
    public String control(){
        try {
            String deviceId = "CL202004080020";
            int interval = 15;
            String settingContent = "laser_cycle_time="+interval;
            byte[] bytes = settingContent.getBytes();
            ByteString byteString = ByteString.copyFrom(bytes);
//            int bizCode = BusinessType.MESSAGE.getCode();
//            BusinessType businessType = BusinessType.getTypeByCode(bizCode);
            String items = BusinessType.MESSAGE.name() + "*" + HardwareType.WIRELESS_LASER_SENSOR_NEW.name();
            ControlApi.Command command = ControlApi.Command.newBuilder()
                    .setDeviceId(deviceId)
                    .setCmdType(ControlApi.Command.CommandType.SETTING)
                    .setItemJson(items)
                    .setParams(byteString)
                    .build();

            ControlApi.GrpcRequest request = ControlApi.GrpcRequest.newBuilder().setCommand(command).build();
            CtrlServiceGrpc.CtrlServiceFutureStub ctrlServiceFutureStub = GrpcClientUtil.getMixFutureStub("localhost");
            ListenableFuture<ControlApi.GrpcResponse> respFuture = ctrlServiceFutureStub.control(request);
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
            ControlApi.GrpcResponse response = respFuture.get(20, TimeUnit.SECONDS);
            String respJson = response.getRespJson();
            log.info("控制结果：resp = {}", respJson);
            return respJson;
        }catch (TimeoutException ex){
            log.info("控制结果： msg = {}, result = {}", "超时（grpc调用端）", false);
            return null;
        }catch (ExecutionException e) {
            log.info("控制结果： msg = {}, result = {}", e.getCause(), false);
            return null;
        }catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

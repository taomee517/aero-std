package com.aero.std.grpc;

import com.aero.beans.constants.FunctionType;
import com.aero.beans.constants.RequestType;
import lombok.Builder;
import lombok.Data;

/**
 * @author 罗涛
 * @title ReplySign
 * @date 2020/6/11 10:58
 */
@Data
@Builder
public class ReplySign {
    private int orderId;
    private String imei;
    private RequestType requestType;
    private FunctionType functionType;
}

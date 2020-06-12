package com.aero.std.grpc;

import com.aero.beans.constants.FunctionType;
import com.aero.beans.constants.RequestType;
import lombok.Data;

/**
 * @author 罗涛
 * @title Order
 * @date 2020/6/11 10:18
 */
@Data
public class Order {
    private int orderId;
    private String imei;
    private RequestType requestType;
    private FunctionType functionType;
    private byte[] params;
}

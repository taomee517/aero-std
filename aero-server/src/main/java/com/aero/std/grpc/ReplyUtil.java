package com.aero.std.grpc;

import com.aero.beans.base.Header;
import com.aero.beans.base.Message;
import com.aero.beans.constants.RequestType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 罗涛
 * @title ReplySignBuilder
 * @date 2020/6/11 11:00
 */
public class ReplyUtil {
    public static ReplySign buildReplySign(Order order){
        RequestType ackType = RequestType.getRequestType(order.getRequestType().getAckCode());
        return ReplySign.builder()
                .orderId(order.getOrderId())
                .imei(order.getImei())
                .requestType(ackType)
                .functionType(order.getFunctionType())
                .build();
    }

    public static CompletableFuture signMatch(ConcurrentMap<ReplySign, CompletableFuture> promiseMap, Message msg) {
        Header header = msg.getHeader();
        for (ReplySign sign : promiseMap.keySet()) {
            //非ack消息
            if (header.getRequest().getAckCode()!=-1) {
                continue;
            }
            if (sign.getOrderId()!= header.getRequestId()) {
                continue;
            }
            if (!sign.getImei().equals(header.getImei())) {
                continue;
            }
            if (!sign.getRequestType().equals(header.getRequest())) {
                continue;
            }
            if (!sign.getFunctionType().equals(header.getFun())) {
                continue;
            }
            return promiseMap.get(sign);
        }
        return null;
    }
}

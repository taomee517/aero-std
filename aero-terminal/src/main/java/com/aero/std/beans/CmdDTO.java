package com.aero.std.beans;

import lombok.Data;

/**
 * @author 罗涛
 * @title CmdDTO
 * @date 2020/6/11 14:30
 */
@Data
public class CmdDTO {
    private String imei;
    private int requestType;
    private int functionType;
    private String paramsJson;
}

package com.aero.beans.base;

import lombok.Data;

import java.util.List;

/**
 * @author 罗涛
 * @title Message
 * @date 2020/5/12 17:19
 */
@Data
public class Message {
    private Header header;
    private Body body;
}

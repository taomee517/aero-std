package com.aero.std.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author 罗涛
 * @title HandlerCenter
 * @date 2020/5/8 11:19
 */
@Component
public class HandlerCenter {

    @Autowired
    public TcpDispatchHandler tcpDispatchHandler;

    @Autowired
    public UnescapeHandler unescapeHandler;

    @Autowired
    public ContentValidateHandler contentValidateHandler;

    @Autowired
    public HeaderParseHandler headerParseHandler;

    @Autowired
    public CoreParseHandler coreParseHandler;

    @Autowired
    public EscapeHandler escapeHandler;

    @Autowired
    public FrameEncoder frameEncoder;
}

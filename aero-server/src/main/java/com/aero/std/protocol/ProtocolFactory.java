package com.aero.std.protocol;

import com.aero.std.context.ProtocolContext;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;

/**
 * @author 罗涛
 * @title ProtocolFactory
 * @date 2020/5/8 14:34
 */
public class ProtocolFactory {
    public static Set<IProtocol> ALL_TCPS;

    @PostConstruct
    public void getAllProtocols(){
        Map<String, IProtocol> protocols = ProtocolContext.context.getBeansOfType(IProtocol.class);
        for(IProtocol protocol: protocols.values()){
            ALL_TCPS.add(protocol);
        }
    }
}

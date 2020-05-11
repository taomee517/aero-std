package com.aero.std.context;

import com.aero.std.protocol.IProtocol;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 罗涛
 * @title SpringContextUtil
 * @date 2020/5/8 14:42
 */
@Configuration
public class ProtocolContext implements ApplicationContextAware {
    public static ApplicationContext context;
    private static Map<Integer, IProtocol> PORT_PROTOCOL_MAP = new HashMap<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static Set<IProtocol> getAllProtocols(){
        Set<IProtocol> allTcps = new HashSet<>();
        Map<String, IProtocol> protocols = context.getBeansOfType(IProtocol.class);
        for(IProtocol protocol: protocols.values()){
            allTcps.add(protocol);
            PORT_PROTOCOL_MAP.put(protocol.port(),protocol);
        }
        return allTcps;
    }

}

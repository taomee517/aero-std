package com.aero.std.context;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author 罗涛
 * @title SpringContextUtil
 * @date 2020/5/8 14:42
 */
public class SpringContextUtil implements ApplicationContextAware {
    public static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public Object getBean(String name){
        return context.getBean(name);
    }

    public <T>T getBean(Class<? extends T> clazz){
        return context.getBean(clazz);
    }

}

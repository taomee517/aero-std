package com.aero.std.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 罗涛
 * @title IndexController
 * @date 2020/5/8 11:04
 */

@RestController
@RequestMapping(value = "/index")
public class IndexController {

    @GetMapping("hello")
    public String sayHello(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Hello,Aero Server! RealTime:" + sdf.format(new Date());
    }
}

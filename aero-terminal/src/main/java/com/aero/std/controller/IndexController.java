package com.aero.std.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author 罗涛
 * @title IndexController
 * @date 2020/6/9 17:37
 */
@RestController
@RequestMapping(value = "/index")
@Api(tags = "测试接口", value = "IndexController")
public class IndexController {

    @GetMapping("hello")
    @ApiOperation("测试方法")
    public String sayHello(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "Hello,Aero Terminal! RealTime:" + sdf.format(new Date());
    }
}

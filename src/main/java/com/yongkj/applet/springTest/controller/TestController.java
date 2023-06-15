package com.yongkj.applet.springTest.controller;

import com.yongkj.applet.springTest.service.TestService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/test")
public class TestController {

    @Resource
    private TestService testService;

    @RequestMapping("/demo")
    public String demo() {
        return testService.demo();
    }

}

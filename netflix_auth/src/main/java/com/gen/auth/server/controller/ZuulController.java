package com.gen.auth.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gen.auth.server.service.ZuulService;
import com.netflix.eureka.bean.ZRsa;
import com.netflix.eureka.command.CommandResponse;

@RestController
@RequestMapping("zuul")
public class ZuulController {
    @Autowired
    private ZuulService service;

    @PostMapping(value = "/rsa")
    public CommandResponse<ZRsa> getUrsa(@RequestParam(name="svrid", required=false) String svrid, @RequestParam(name="ts", required=false) Long ts) {
    	return service.queryRsaBySvrid(svrid, ts);
    }
    
}

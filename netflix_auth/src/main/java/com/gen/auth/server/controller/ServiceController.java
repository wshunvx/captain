package com.gen.auth.server.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gen.auth.server.entity.Client;
import com.gen.auth.server.entity.Instance;
import com.gen.auth.server.entity.Svrtype;
import com.gen.auth.server.service.AuthClientService;
import com.netflix.eureka.command.CommandResponse;

@RestController
@RequestMapping("server")
public class ServiceController {
    @Autowired
    private AuthClientService service;

    @GetMapping(value = "/instance")
    public CommandResponse<List<Client>> getInstance(@RequestParam Map<String, Object> params) {
    	return service.getClientAll(params);
    }
    
    @PostMapping(value = "/instance")
    public CommandResponse<Client> setInstance(@RequestBody Client client) {
    	return service.setClientAll(client);
    }
    
    @GetMapping(value = "/svrtype")
    public CommandResponse<List<Svrtype>> getSvrtype() {
    	return service.getSvrtype();
    }
    
    @GetMapping(value = "/namespace")
    public CommandResponse<List<Instance>> getNamespace() {
    	return service.getInstance();
    }
    
    @PostMapping(value = "/namespace")
    public CommandResponse<Instance> setNamespace(@RequestBody Instance namespace) {
    	return service.setInstance(namespace);
    }
    
}

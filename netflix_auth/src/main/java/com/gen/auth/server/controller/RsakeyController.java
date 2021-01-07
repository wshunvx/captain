package com.gen.auth.server.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.gen.auth.server.entity.Uri;
import com.gen.auth.server.entity.Ursa;
import com.gen.auth.server.service.RsakeyService;
import com.netflix.eureka.command.CommandResponse;

@RestController
@RequestMapping("rsakey")
public class RsakeyController {
    @Autowired
    private RsakeyService service;

    @GetMapping(value = "/uris")
    public CommandResponse<List<Uri>> getAuthUri() {
    	return service.getAuthUri();
    }
    
    @PostMapping(value = "/uris")
    public CommandResponse<String> postAuthUri(@RequestBody Uri uri) {
    	if(uri == null || (StringUtils.isEmpty(uri.getBasepath()) 
    			|| StringUtils.isEmpty(uri.getSvrid()))) {
    		return CommandResponse.ofFailure("Invalid parameter.");
    	}
    	return service.insertOrUpdateAuthUri(uri);
    }
    
    @DeleteMapping(value = "/uris")
    public CommandResponse<String> deleteAuthUri(@RequestParam("id") String id) {
    	return service.removeAuthUri(id);
    }
    
    @GetMapping(value = "/first")
    public CommandResponse<Ursa> findFirst() {
    	return service.getUserRsa();
    }
    
    @GetMapping(value = "/reset")
    public CommandResponse<Ursa> resetKey() {
    	return service.resetUserRsa();
    }
    
    @GetMapping(value = "/findAll")
    public CommandResponse<List<Ursa>> findAll() {
    	return service.getUserRsaList();
    }
    
    @PostMapping(value = "/keys")
    public CommandResponse<String> insert(@RequestBody Ursa ursa) {
    	if(ursa == null || (StringUtils.isEmpty(ursa.getName()) || StringUtils.isEmpty(ursa.getSeeded()))) {
    		return CommandResponse.ofFailure("Invalid parameter."); 
    	}
    	return service.insertOrUpdateUserRsa(ursa);
    }
    
}

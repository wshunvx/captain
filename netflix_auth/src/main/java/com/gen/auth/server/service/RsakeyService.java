package com.gen.auth.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.gen.auth.server.biz.UriBiz;
import com.gen.auth.server.biz.UrsaBiz;
import com.gen.auth.server.entity.Uri;
import com.gen.auth.server.entity.Ursa;
import com.netflix.eureka.command.CommandResponse;

@Service
public class RsakeyService {
    @Autowired
    private UriBiz uriBiz;
    @Autowired
    private UrsaBiz ursaBiz;
    
    private final static String SOURCES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890~!@#$%^&*()";
    
    public CommandResponse<List<Uri>> getAuthUri() {
    	List<Uri> list = uriBiz.list(new QueryWrapper<Uri>());
    	if(list == null) {
    		list = new ArrayList<Uri>();
    	}
    	return CommandResponse.ofSuccess(list);
    }
    
    public CommandResponse<String> insertOrUpdateAuthUri(Uri uri) {
    	if(uri.getStrategy() == null) {
    		uri.setStrategy(0);
    	}
    	
    	if(StringUtils.isEmpty(uri.getId())) {
    		uri.setStatus(0);
        	uri.setCreatetime(new Date());
        	int status = uriBiz.save(uri);
        	if(status > 0) {
        		return CommandResponse.ofSuccess("Ok");
        	}
        	return CommandResponse.ofFailure("Save fail.");
    	}
    	
    	String id = uri.getId();
    	if(StringUtils.isEmpty(id)) {
    		return CommandResponse.ofFailure("Id null.");
    	}
    	
    	Uri u = uriBiz.getById(id);
    	if(u == null) {
    		return CommandResponse.ofFailure("No record.");
    	}
    	
    	if(uri.getStrategy() == null) {
    		uri.setStrategy(u.getStrategy());
    	}
    	
    	if(StringUtils.isEmpty(uri.getMethod())) {
    		uri.setMethod(u.getMethod());
    	}
    	
    	if(StringUtils.isEmpty(uri.getSummary())) {
    		uri.setSummary(u.getSummary());
    	}
    	
    	if(uri.getStatus() == null) {
    		uri.setStatus(u.getStatus());
    	}
    	
    	uri.setUpdatetime(new Date());
    	int status = uriBiz.updateById(uri);
    	if(status > 0) {
    		return CommandResponse.ofSuccess("Ok");
    	}
    	return CommandResponse.ofFailure("Update fail.");
    }
    
    public CommandResponse<String> removeAuthUri(String id) {
    	Uri entity = uriBiz.getById(id);
    	if(entity == null) {
    		return CommandResponse.ofFailure("No record.");
    	}
    	
    	entity.setStatus(1);
    	entity.setUpdatetime(new Date());
    	int status = uriBiz.updateById(entity);
    	if(status > 0) {
    		return CommandResponse.ofSuccess("Ok");
    	}
    	return CommandResponse.ofFailure("Save fail.");
    }
    
	public CommandResponse<Ursa> getUserRsa() {
		QueryWrapper<Ursa> example = new QueryWrapper<>();
		example.eq("status", 2);
		
		List<Ursa> list = ursaBiz.list(example);
		if(list == null || list.isEmpty()) {
			return CommandResponse.ofFailure("Ursa items null.");
		}
		
		Ursa ursa = list.stream().filter(pre -> {
			Date expired = pre.getExpired();
			if(expired == null) {
				return true;
			}
			if(expired.getTime() < System.currentTimeMillis()) {
				return true;
			}
			return false;
		}).findFirst().get();
		
		if(ursa != null) {
			return CommandResponse.ofSuccess(ursa);
		}
		
		return CommandResponse.ofFailure("");
	}
	
	public CommandResponse<Ursa> resetUserRsa() {
		QueryWrapper<Ursa> example = new QueryWrapper<>();
		example.eq("status", 2);
		
		List<Ursa> list = ursaBiz.list(example);
		if(list == null || list.isEmpty()) {
			return CommandResponse.ofFailure("Ursa items null.");
		}
		
		Ursa ursa = list.stream().filter(pre -> {
			Date expired = pre.getExpired();
			if(expired == null) {
				return true;
			}
			if(expired.getTime() < System.currentTimeMillis()) {
				return true;
			}
			return false;
		}).findFirst().get();
		
		if(ursa != null) {
			ursa.setSeeded(generateString(SOURCES, 16));
			ursa.setUpdatetime(new Date());
			int up = ursaBiz.updateById(ursa);
			if(up > 0) {
				return CommandResponse.ofSuccess(ursa);
			}
		}
		
		return CommandResponse.ofFailure("");
	}
	
	public CommandResponse<List<Ursa>> getUserRsaList() {
		List<Ursa> list = ursaBiz.list(new QueryWrapper<Ursa>());
		if(list == null || list.isEmpty()) {
			return CommandResponse.ofFailure("");
		}
		return CommandResponse.ofSuccess(list);
	}
	
	public CommandResponse<String> insertOrUpdateUserRsa(Ursa ursa) {
		if(StringUtils.isEmpty(ursa.getId())) {
			ursa.setCreatetime(new Date());
			ursa.setStatus(0);
			int up = ursaBiz.save(ursa);
			if(up > 0) {
				return CommandResponse.ofSuccess("Ok");
			}
			
			return CommandResponse.ofFailure("Save fail.");
		}
		
		Ursa entity = ursaBiz.getById(ursa.getId());
		if(entity == null) {
			return CommandResponse.ofFailure("Status old eq");
		}
		
		int up = 0;
		ursa.setUpdatetime(new Date());
		if(ursa.getStatus() == 2) {
			QueryWrapper<Ursa> example = new QueryWrapper<>();
			example.eq("status", 2);
			List<Ursa> list = ursaBiz.list(example);
			if(list == null || list.isEmpty()) {
				up = ursaBiz.updateById(ursa);
			} else {
				UpdateWrapper<Ursa> update = new UpdateWrapper<>();
				update.in("id", list.stream().map(Ursa::getId).collect(Collectors.toList()));
				Ursa upStatus = new Ursa();
				upStatus.setStatus(1);
				int num = ursaBiz.update(upStatus, example);
				if(num == list.size()) {
					up = ursaBiz.updateById(ursa);
				}
			}
		} else {
			up = ursaBiz.updateById(ursa);
		}
		
		if(up > 0) {
			return CommandResponse.ofSuccess("Ok");
		}
		
		return CommandResponse.ofFailure("Update fail.");
	}
	
	private String generateString(String characters, int length) {
		Random random = new Random();
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = characters.charAt(random.nextInt(characters.length()));
        }
        return new String(text);
    }
}

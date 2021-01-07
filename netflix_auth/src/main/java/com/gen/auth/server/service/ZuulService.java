package com.gen.auth.server.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.gen.auth.server.biz.UriBiz;
import com.gen.auth.server.biz.UrsaBiz;
import com.gen.auth.server.entity.Uri;
import com.gen.auth.server.entity.Ursa;
import com.netflix.eureka.bean.ZRsa;
import com.netflix.eureka.command.CommandResponse;

@Service
public class ZuulService {
	@Autowired
	private UriBiz uriBiz;
	@Autowired
	private UrsaBiz ursaBiz;
	
	public CommandResponse<ZRsa> queryRsaBySvrid(String svrid, Long ts) {
		ZRsa.Builder builder = ZRsa.Builder.newBuilder(System.currentTimeMillis());
		QueryWrapper<Ursa> rsaExample = new QueryWrapper<>();
		rsaExample.eq("status", 2);
		rsaExample.last("limit 1");
		List<Ursa> rsas = ursaBiz.list(rsaExample);
		if(rsas == null || rsas.isEmpty()) {
			return CommandResponse.ofFailure("Ursa items null.");
		}
		
		Ursa ursa = rsas.stream().filter(pre -> {
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
			builder.setPassword(ursa.getSeeded());
		}
		
		List<Uri> uris = uriBiz.queryAll();
    	if(uris == null) {
    		uris = new ArrayList<Uri>();
    	}
    	uris.stream().filter(pre -> {
    		Date date = pre.getUpdatetime();
			if(date == null) {
				date = pre.getCreatetime();
			}
			if(date == null) {
				return true;
			}
			if(!(ts == null || ts < 1)) {
				if(ts < date.getTime()) {
					return true;
				}
				return false;
			}
			return true;
    	}).forEach(u -> builder.setUri(u.getSvrid(), u.getId(), u.getBasepath(), u.getStrategy(), u.getMethod()));
    	return CommandResponse.ofSuccess(builder.build());
	}
}

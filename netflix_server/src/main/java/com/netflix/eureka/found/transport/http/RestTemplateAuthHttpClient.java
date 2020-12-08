package com.netflix.eureka.found.transport.http;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.model.Namespace;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.found.transport.rule.AuthRule;

@SuppressWarnings("rawtypes")
public class RestTemplateAuthHttpClient implements AuthHttpClient {
	private AuthRule aRule;
	private RestTemplate restTemplate;

	public RestTemplateAuthHttpClient(RestTemplate restTemplate, AuthRule aRule) {
		this.restTemplate = restTemplate;
		this.aRule = aRule;
	}
	
	@Override
	public Response getNamespace() {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/namespace";

		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}
	
	
	
	@Override
	public Response getSvrtype() {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/svrtype";

		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response setNamespace(Namespace namespace) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/namespace";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		ResponseEntity<Restresult> response = restTemplate.exchange(urlPath, HttpMethod.POST,
				new HttpEntity<>(namespace, headers), Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response getClient(String instanceId, String svrid) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/instance";
		Set<String> urlParm = new HashSet<String>();
		if(instanceId != null) {
			urlParm.add("instance_id=" + instanceId);
		}
		if(svrid != null) {
			urlParm.add("svrid=" + svrid);
		}
		if(!urlParm.isEmpty()) {
			urlPath += "?" + String.join("&", urlParm);
		}
		
		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response setClient(Cluster cluster) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/instance";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		ResponseEntity<Restresult> response = restTemplate.exchange(urlPath, HttpMethod.POST,
				new HttpEntity<>(cluster, headers), Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

}

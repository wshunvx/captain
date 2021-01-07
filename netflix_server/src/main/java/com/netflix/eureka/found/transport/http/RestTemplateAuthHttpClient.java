package com.netflix.eureka.found.transport.http;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.Response;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.google.common.reflect.TypeToken;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.eureka.bean.ZClient;
import com.netflix.eureka.found.model.Cluster;
import com.netflix.eureka.found.model.Namespace;
import com.netflix.eureka.found.model.Restresult;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.found.transport.rule.AuthRule;
import com.netflix.eureka.gson.JSONFormatter;

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
	public Response getClient(String namespaceId, String svrid) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/instance";
		Set<String> urlParm = new HashSet<String>();
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
	public Restresult<ZClient> getClient(String instanceId) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return null;
		}
		
		
		String urlPath = instanceInfo.getHomePageUrl() + "server/instance/findById?instance_id=" + instanceId;
		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return null;
		}
		
		Type typeOfSrc = new TypeToken<Restresult<ZClient>>(){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
		}.getType();
		
		return JSONFormatter.fromJSON(response.getBody().toString(), typeOfSrc);
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

	@Override
	public Response getRsaUris() {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/uris";
		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response setRsaUris(String id, String summary, String svrid, String basepath, String strategy, String method) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/uris";
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		Map<String, Object> rsa = new HashMap<String, Object>();
		rsa.put("id", id);
		rsa.put("summary", summary);
		rsa.put("svrid", svrid);
		rsa.put("basepath", basepath);
		rsa.put("strategy", strategy);
		rsa.put("method", method);
		ResponseEntity<Restresult> response = restTemplate.exchange(urlPath, HttpMethod.POST,
				new HttpEntity<>(rsa, headers), Restresult.class);
		
		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response delRsaUris(String id) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/uris?id=" + id;
		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		ResponseEntity<Restresult> response = restTemplate.exchange(urlPath, HttpMethod.DELETE,
				null, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response getRsaFirst() {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/first";
		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response getRsaReset() {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/reset";
		ResponseEntity<Restresult> response = restTemplate.getForEntity(urlPath, Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	@Override
	public Response setUserRsa(String id, String name, String seeded, Date expired) {
		InstanceInfo instanceInfo = aRule.choose();
		if(instanceInfo == null) {
			return Response.serverError().build();
		}
		
		String urlPath = instanceInfo.getHomePageUrl() + "rsakey/keys";

		HttpHeaders headers = new HttpHeaders();
		headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		
		Map<String, Object> rsa = new HashMap<String, Object>();
		rsa.put("id", id);
		rsa.put("name", name);
		rsa.put("seeded", seeded);
		rsa.put("expired", expired);
		ResponseEntity<Restresult> response = restTemplate.exchange(urlPath, HttpMethod.POST,
				new HttpEntity<>(rsa, headers), Restresult.class);

		if(response == null || response.getStatusCodeValue() != 200) {
			return Response.ok().build();
		}
		
		return Response.ok(response.getBody()).build();
	}

	
}

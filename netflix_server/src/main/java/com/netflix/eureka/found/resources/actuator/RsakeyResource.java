package com.netflix.eureka.found.resources.actuator;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.springframework.util.StringUtils;

import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.time.DateUtil;

@Produces({"application/xml", "application/json"})
public class RsakeyResource {

    private AuthHttpClient httpClient;

    RsakeyResource(AuthHttpClient httpClient) {
    	this.httpClient = httpClient;
    }
    
    @GET
    public Response getNamespaces() {
		return httpClient.getRsaFirst();
    }

    @POST
    public Response addApi(MultivaluedMap<String, String> query) {
    	String expired = query.getFirst("expired");
    	if(StringUtils.isEmpty(expired)) {
    		return httpClient.setUserRsa(
    				query.getFirst("id"), 
    				query.getFirst("name"), 
    				query.getFirst("seeded"), 
    				null);
    	}
    	return httpClient.setUserRsa(
    			query.getFirst("id"), 
    			query.getFirst("name"), 
    			query.getFirst("seeded"), 
    			DateUtil.str2date(expired, DateUtil.YYYY_MM_DD));
    }

}

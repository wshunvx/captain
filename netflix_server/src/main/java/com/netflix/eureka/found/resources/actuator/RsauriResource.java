package com.netflix.eureka.found.resources.actuator;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.transport.AuthHttpClient;

@Produces({"application/xml", "application/json"})
public class RsauriResource {

    private AuthHttpClient httpClient;

    RsauriResource(AuthHttpClient httpClient) {
    	this.httpClient = httpClient;
    }
    
    @GET
    public Response getNamespaces() {
		return httpClient.getRsaUris();
    }

    @POST
    public Response addApi(MultivaluedMap<String, String> query) {
    	return httpClient.setRsaUris(
    			query.getFirst("id"), 
    			query.getFirst("summary"), 
    			query.getFirst("svrid"), 
    			query.getFirst("basepath"), 
    			query.getFirst("strategy"), 
    			query.getFirst("method"));
    }

    @DELETE
    public Response deleteApi(@QueryParam("id") String id) {
    	return httpClient.delRsaUris(id);
    }

}

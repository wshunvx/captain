package com.netflix.eureka.found.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Path("/{version}/svrtype")
@Produces({"application/xml", "application/json"})
public class SvrtypeResource {
	
	private final AuthHttpClient httpClient;
	
	@Inject
	SvrtypeResource(SentinelServerContext serverContext) {
        this.httpClient = serverContext.getAuthHttpClient();
    }

    public SvrtypeResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
	@GET
    public Response getNamespaces() {
		return httpClient.getSvrtype();
    }
}

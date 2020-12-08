package com.netflix.eureka.found.resources;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Path("/{version}/namespaces")
@Produces({"application/xml", "application/json"})
public class NamespacesResource {
	
	private final AuthHttpClient httpClient;
	
	@Inject
	NamespacesResource(SentinelServerContext serverContext) {
        this.httpClient = serverContext.getAuthHttpClient();
    }

    public NamespacesResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
    
	@GET
    public Response getNamespaces() {
		return httpClient.getNamespace();
    }
}

package com.netflix.eureka.found.resources.actuator;

import javax.inject.Inject;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;

@Path("/{version}/authority")
@Produces({"application/xml", "application/json"})
public class AuthorityRuleResource {

    private AuthHttpClient httpClient;

    @Inject
    AuthorityRuleResource(ServerContext serverContext) {
    	this.httpClient = serverContext.getAuthHttpClient();
    }

    public AuthorityRuleResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
    
    @Path("rsakey")
    public RsakeyResource getRsakeyResource() {
    	return new RsakeyResource(httpClient);
    }
    
    @Path("rsauri")
    public RsauriResource getConfigurationMenuResource() {
    	return new RsauriResource(httpClient);
    }
    
    @PUT
    public Response updateApi() {
    	return httpClient.getRsaReset();
    }
    
}

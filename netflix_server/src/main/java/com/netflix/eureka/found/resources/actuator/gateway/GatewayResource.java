package com.netflix.eureka.found.resources.actuator.gateway;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.found.repository.gateway.InMemApiDefinitionStore;
import com.netflix.eureka.found.repository.gateway.InMemGatewayFlowRuleStore;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/gateway")
@Produces({"application/xml", "application/json"})
public class GatewayResource {
    private SentinelApiClient apiClient;
    private PeerAwareInstanceRegistry registry;
    
    private InMemApiDefinitionStore inMemApiDefinition;
    private InMemGatewayFlowRuleStore inMemGatewayFlowRule;
	
    @Inject
    GatewayResource(SentinelServerContext serverContext) {
    	this.apiClient = serverContext.getSentinelApiClient();
        this.registry = serverContext.getInstanceRegistry();
        this.inMemApiDefinition = serverContext.getInMemApiDefinition();
        this.inMemGatewayFlowRule = serverContext.getInMemGatewayFlowRule();
    }

    public GatewayResource() {
        this(SentinelServerContextHolder.getSentinel().getServerContext());
    }
	
	@Path("api")
    public GatewayApiResource getGatewayApi() {
		return new GatewayApiResource(apiClient, registry, inMemApiDefinition);
    }
	
	@Path("flow")
    public GatewayFlowRuleResource getGatewayFlowRule() {
		return new GatewayFlowRuleResource(apiClient, registry, inMemGatewayFlowRule);
    }
	
}

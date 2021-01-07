package com.netflix.eureka.found.resources.actuator.gateway;

import javax.inject.Inject;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Path("/{version}/gateway")
@Produces({"application/xml", "application/json"})
public class GatewayResource {
    private HttpapiClient apiClient;
    private PeerAwareInstanceRegistry registry;
    private RuleRepositoryAdapter<ApiDefinitionEntity> apiDefinition;
    private RuleRepositoryAdapter<GatewayFlowRuleEntity> gatewayFlowRule;
    
    @Inject
    GatewayResource(ServerContext serverContext) {
    	this.apiClient = serverContext.getHttpapiClient();
        this.registry = serverContext.getInstanceRegistry();
        this.apiDefinition = serverContext.getApiDefinition();
        this.gatewayFlowRule = serverContext.getGatewayFlowRule();
    }

    public GatewayResource() {
        this(ServerContextHolder.getSecurity().getServerContext());
    }
	
	@Path("api")
    public GatewayApiResource getGatewayApi() {
		return new GatewayApiResource(apiClient, registry, apiDefinition);
    }
	
	@Path("flow")
    public GatewayFlowRuleResource getGatewayFlowRule() {
		return new GatewayFlowRuleResource(apiClient, registry, gatewayFlowRule);
    }
	
}

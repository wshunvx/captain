package com.netflix.eureka.found.sentinel;

import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.found.registry.ServiceGenerator;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public interface ServerContext {
	void initialize() throws Exception;

	AuthHttpClient getAuthHttpClient();
    HttpapiClient getHttpapiClient();
    
    ServiceGenerator getServiceGenerator();
    
    RuleRepositoryAdapter<FlowRuleEntity> getFlowRule();
//	RuleRepositoryAdapter<AuthorityRuleEntity> getAuthorityRule();
	RuleRepositoryAdapter<DegradeRuleEntity> getDegradeRule();
	RuleRepositoryAdapter<ApiDefinitionEntity> getApiDefinition();
	RuleRepositoryAdapter<GatewayFlowRuleEntity> getGatewayFlowRule();
	RuleRepositoryAdapter<SystemRuleEntity> getSystemRule();
	RuleRepositoryAdapter<ParamFlowRuleEntity> getParamFlowRule();
    
    PeerAwareInstanceRegistry getInstanceRegistry();
    
}

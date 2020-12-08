package com.netflix.eureka.found.sentinel;

import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.MetricEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.dashboard.repository.metric.MetricsRepository;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.dashboard.service.ClusterService;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;
import com.netflix.eureka.found.repository.gateway.InMemApiDefinitionStore;
import com.netflix.eureka.found.repository.gateway.InMemGatewayFlowRuleStore;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public interface SentinelServerContext {
	void initialize() throws Exception;

	ClusterService getService();
	
	AuthHttpClient getAuthHttpClient();
    SentinelApiClient getSentinelApiClient();
    
    PeerAwareInstanceRegistry getInstanceRegistry();
    
    MetricsRepository<MetricEntity> getMetri();
    RuleRepository<SystemRuleEntity> getSystemRule();
    RuleRepository<DegradeRuleEntity> getDegradeRule();
    RuleRepository<ParamFlowRuleEntity> getParamFlowRule();
    RuleRepository<AuthorityRuleEntity> getAuthorityRule();
    InMemoryRuleRepositoryAdapter<FlowRuleEntity> getInMemoryRule();

    InMemApiDefinitionStore getInMemApiDefinition();
	InMemGatewayFlowRuleStore getInMemGatewayFlowRule();
}

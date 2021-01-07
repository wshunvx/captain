package com.netflix.eureka.found.service;

import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.dashboard.datasource.entity.gateway.ApiDefinitionEntity;
import com.netflix.eureka.dashboard.datasource.entity.gateway.GatewayFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.found.registry.ServiceGenerator;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;
import com.netflix.eureka.found.repository.RuleRepositoryAdapter;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.sentinel.ServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.found.transport.AuthTransportClientFactory;
import com.netflix.eureka.found.transport.rule.RandomRule;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public class ServerContextImpl implements ServerContext {
	
	protected PeerAwareInstanceRegistry registry;
	
	protected AuthHttpClient authHttp;
	protected HttpapiClient sentinelApi;
	protected ServiceGenerator generator;
	
	protected RuleRepositoryAdapter<FlowRuleEntity> flowRule;
//	protected RuleRepositoryAdapter<AuthorityRuleEntity> authorityRule;
	protected RuleRepositoryAdapter<DegradeRuleEntity> degradeRule;
	protected RuleRepositoryAdapter<ApiDefinitionEntity> apiDefinition;
	protected RuleRepositoryAdapter<GatewayFlowRuleEntity> gatewayFlowRule;
	protected RuleRepositoryAdapter<SystemRuleEntity> systemRule;
	protected RuleRepositoryAdapter<ParamFlowRuleEntity> paramFlowRule;
	
	public ServerContextImpl(PeerAwareInstanceRegistry registry, ServiceGenerator generator, HttpapiClient sentinelApi) {
		this.registry = registry;
		this.generator = generator;
		this.sentinelApi = sentinelApi;
		this.authHttp = new AuthTransportClientFactory().newClient(new HttpRandomRule());
		this.flowRule = new InMemoryRuleRepositoryAdapter<FlowRuleEntity>();
//		this.authorityRule = new InMemoryRuleRepositoryAdapter<AuthorityRuleEntity>();
		this.degradeRule = new InMemoryRuleRepositoryAdapter<DegradeRuleEntity>();
		this.apiDefinition = new InMemoryRuleRepositoryAdapter<ApiDefinitionEntity>();
		this.gatewayFlowRule = new InMemoryRuleRepositoryAdapter<GatewayFlowRuleEntity>();
		this.systemRule = new InMemoryRuleRepositoryAdapter<SystemRuleEntity>();
		this.paramFlowRule = new InMemoryRuleRepositoryAdapter<ParamFlowRuleEntity>();
	}
	
	@Override
	public void initialize() throws Exception {
		ServerContextHolder.initialize(this);
	}

	@Override
	public AuthHttpClient getAuthHttpClient() {
		return authHttp;
	}

	@Override
	public HttpapiClient getHttpapiClient() {
		return sentinelApi;
	}
	
	@Override
	public ServiceGenerator getServiceGenerator() {
		return generator;
	}

	@Override
	public PeerAwareInstanceRegistry getInstanceRegistry() {
		return registry;
	}

	@Override
	public RuleRepositoryAdapter<FlowRuleEntity> getFlowRule() {
		return flowRule;
	}

//	@Override
//	public RuleRepositoryAdapter<AuthorityRuleEntity> getAuthorityRule() {
//		return authorityRule;
//	}

	@Override
	public RuleRepositoryAdapter<DegradeRuleEntity> getDegradeRule() {
		return degradeRule;
	}

	@Override
	public RuleRepositoryAdapter<ApiDefinitionEntity> getApiDefinition() {
		return apiDefinition;
	}

	@Override
	public RuleRepositoryAdapter<GatewayFlowRuleEntity> getGatewayFlowRule() {
		return gatewayFlowRule;
	}

	@Override
	public RuleRepositoryAdapter<SystemRuleEntity> getSystemRule() {
		return systemRule;
	}

	@Override
	public RuleRepositoryAdapter<ParamFlowRuleEntity> getParamFlowRule() {
		return paramFlowRule;
	}

	class HttpRandomRule extends RandomRule {

		@Override
		public PeerAwareInstanceRegistry getPeerAwareInstanceRegistry() {
			return registry;
		}
		
	}

}

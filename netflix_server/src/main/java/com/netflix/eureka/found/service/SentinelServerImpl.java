package com.netflix.eureka.found.service;

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
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.sentinel.SentinelServerContextHolder;
import com.netflix.eureka.found.transport.AuthHttpClient;
import com.netflix.eureka.found.transport.AuthTransportClientFactory;
import com.netflix.eureka.found.transport.rule.RandomRule;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

public class SentinelServerImpl implements SentinelServerContext {
	
	protected ClusterService service;
	
	protected PeerAwareInstanceRegistry registry;
	
	protected AuthHttpClient authHttp;
	protected SentinelApiClient sentinelApi;
	
	InMemApiDefinitionStore inMemApiDefinition;
	InMemGatewayFlowRuleStore inMemGatewayFlowRule;
	
	protected MetricsRepository<MetricEntity> metric;
	protected RuleRepository<SystemRuleEntity> systemRule;
	protected RuleRepository<DegradeRuleEntity> degradeRule;
	protected RuleRepository<ParamFlowRuleEntity> paramFlowRule;
	protected RuleRepository<AuthorityRuleEntity> authorityRule;
	protected InMemoryRuleRepositoryAdapter<FlowRuleEntity> inMemoryRule;
	
	public SentinelServerImpl(PeerAwareInstanceRegistry registry, SentinelApiClient sentinelApi,
			MetricsRepository<MetricEntity> metric,
			RuleRepository<SystemRuleEntity> systemRule,
			RuleRepository<DegradeRuleEntity> degradeRule,
			RuleRepository<ParamFlowRuleEntity> paramFlowRule,
			RuleRepository<AuthorityRuleEntity> authorityRule,
			InMemoryRuleRepositoryAdapter<FlowRuleEntity> inMemoryRule,
			InMemApiDefinitionStore inMemApiDefinition,
			InMemGatewayFlowRuleStore inMemGatewayFlowRule) {
		this.registry = registry;
		this.metric = metric;
		this.systemRule = systemRule;
		this.degradeRule = degradeRule;
		this.paramFlowRule = paramFlowRule;
		this.authorityRule = authorityRule;
		this.inMemoryRule = inMemoryRule;
		this.inMemApiDefinition = inMemApiDefinition;
		this.inMemGatewayFlowRule = inMemGatewayFlowRule;
		this.sentinelApi = sentinelApi;
		this.service = new ClusterServiceImpl(sentinelApi, registry);
		this.authHttp = new AuthTransportClientFactory().newClient(new HttpRandomRule());
	}
	
	@Override
	public void initialize() throws Exception {
		SentinelServerContextHolder.initialize(this);
	}

	@Override
	public ClusterService getService() {
		return service;
	}

	@Override
	public AuthHttpClient getAuthHttpClient() {
		return authHttp;
	}

	@Override
	public SentinelApiClient getSentinelApiClient() {
		return sentinelApi;
	}
	
	@Override
	public MetricsRepository<MetricEntity> getMetri() {
		return metric;
	}

	@Override
	public RuleRepository<SystemRuleEntity> getSystemRule() {
		return systemRule;
	}
	
	@Override
	public RuleRepository<DegradeRuleEntity> getDegradeRule() {
		return degradeRule;
	}

	@Override
	public RuleRepository<ParamFlowRuleEntity> getParamFlowRule() {
		return paramFlowRule;
	}

	@Override
	public RuleRepository<AuthorityRuleEntity> getAuthorityRule() {
		return authorityRule;
	}

	@Override
	public InMemoryRuleRepositoryAdapter<FlowRuleEntity> getInMemoryRule() {
		return inMemoryRule;
	}

	@Override
	public InMemApiDefinitionStore getInMemApiDefinition() {
		return inMemApiDefinition;
	}

	@Override
	public InMemGatewayFlowRuleStore getInMemGatewayFlowRule() {
		return inMemGatewayFlowRule;
	}

	@Override
	public PeerAwareInstanceRegistry getInstanceRegistry() {
		return registry;
	}

	class HttpRandomRule extends RandomRule {

		@Override
		public PeerAwareInstanceRegistry getPeerAwareInstanceRegistry() {
			return registry;
		}
		
	}

}

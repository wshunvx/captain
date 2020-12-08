package com.netflix.eureka.found.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import com.netflix.eureka.dashboard.client.SentinelApiClient;
import com.netflix.eureka.dashboard.datasource.entity.MetricEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.netflix.eureka.dashboard.datasource.entity.rule.SystemRuleEntity;
import com.netflix.eureka.dashboard.repository.metric.MetricsRepository;
import com.netflix.eureka.dashboard.repository.rule.RuleRepository;
import com.netflix.eureka.found.repository.InMemoryRuleRepositoryAdapter;
import com.netflix.eureka.found.repository.gateway.InMemApiDefinitionStore;
import com.netflix.eureka.found.repository.gateway.InMemGatewayFlowRuleStore;
import com.netflix.eureka.found.sentinel.SentinelServerContext;
import com.netflix.eureka.found.service.SentinelServerImpl;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;

@Configuration(proxyBeanMethods = false)
@Import(WebServerInitializerConfiguration.class)
@AutoConfigureAfter(EurekaServerAutoConfiguration.class)
public class WebServerAutoConfiguration {
	
	@Autowired
	MetricsRepository<MetricEntity> metric;
	@Autowired
	RuleRepository<SystemRuleEntity> systemRule;
	@Autowired
	RuleRepository<DegradeRuleEntity> degradeRule;
	@Autowired
	RuleRepository<ParamFlowRuleEntity> paramFlowRule;
	@Autowired
	RuleRepository<AuthorityRuleEntity> authorityRule;
	@Autowired
	InMemoryRuleRepositoryAdapter<FlowRuleEntity> inMemoryRule;
	
	@Autowired
	InMemApiDefinitionStore inMemApiDefinition;
	@Autowired
	InMemGatewayFlowRuleStore inMemGatewayFlowRule;
	
	@Bean
	public SentinelApiClient getSentinelApiClient() {
		return new SentinelApiClient();
	}
	
	@Bean
	public SentinelServerContext iSentinelServerContext(PeerAwareInstanceRegistry registry, SentinelApiClient sentinelApi) {
		return new SentinelServerImpl(registry, sentinelApi, metric, systemRule, degradeRule, paramFlowRule, authorityRule,
				inMemoryRule, inMemApiDefinition, inMemGatewayFlowRule);
	}
	
//	@Bean
//    public FilterRegistrationBean<Filter> sentinelFilterRegistration() {
//        FilterRegistrationBean<Filter> registration = new FilterRegistrationBean<>();
//        registration.setFilter(new CommonFilter());
//        registration.addUrlPatterns("/*");
//        registration.setName("sentinelFilter");
//        registration.setOrder(1);
//        // If this is enabled, the entrance of all Web URL resources will be unified as a single context name.
//        // In most scenarios that's enough, and it could reduce the memory footprint.
//        registration.addInitParameter(CommonFilter.WEB_CONTEXT_UNIFY, "true");
//
//        return registration;
//    }
}

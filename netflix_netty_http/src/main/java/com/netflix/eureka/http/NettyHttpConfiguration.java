package com.netflix.eureka.http;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.web.mappings.servlet.DispatcherServletsMappingDescriptionProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.CloudEurekaClient;
import org.springframework.cloud.netflix.eureka.EurekaClientAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.PidUtil;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.http.handler.BasicInfoCommandHandler;
import com.netflix.eureka.http.handler.FetchActiveRuleCommandHandler;
import com.netflix.eureka.http.handler.FetchClusterNodeByIdCommandHandler;
import com.netflix.eureka.http.handler.FetchClusterNodeHumanCommandHandler;
import com.netflix.eureka.http.handler.FetchJsonTreeCommandHandler;
import com.netflix.eureka.http.handler.FetchOriginCommandHandler;
import com.netflix.eureka.http.handler.FetchSimpleClusterNodeCommandHandler;
import com.netflix.eureka.http.handler.FetchSystemStatusCommandHandler;
import com.netflix.eureka.http.handler.FetchTreeCommandHandler;
import com.netflix.eureka.http.handler.GetParamFlowRulesCommandHandler;
import com.netflix.eureka.http.handler.ModifyParamFlowRulesCommandHandler;
import com.netflix.eureka.http.handler.ModifyRulesCommandHandler;
import com.netflix.eureka.http.handler.ResourceCommandHandler;
import com.netflix.eureka.http.handler.SendMetricCommandHandler;
import com.netflix.eureka.http.handler.VersionCommandHandler;
import com.netflix.eureka.http.handler.cluster.FetchClusterModeCommandHandler;
import com.netflix.eureka.http.handler.cluster.ModifyClusterModeCommandHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "eureka.security.enabled", matchIfMissing = true)
@AutoConfigureBefore(EurekaClientAutoConfiguration.class)
@EnableConfigurationProperties
@PropertySource("classpath:/spring.properties")
public class NettyHttpConfiguration {

	@Bean
	@ConditionalOnMissingBean
	FetchClusterModeCommandHandler fetchClusterModeEndpoint() {
		return new FetchClusterModeCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	ModifyClusterModeCommandHandler modifyClusterModeEndpoint() {
		return new ModifyClusterModeCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	BasicInfoCommandHandler basicInfoEndpoint() {
		return new BasicInfoCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchActiveRuleCommandHandler fetchActiveRuleEndpoint() {
		return new FetchActiveRuleCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchClusterNodeByIdCommandHandler fetchClusterNodeByIdEndpoint() {
		return new FetchClusterNodeByIdCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchClusterNodeHumanCommandHandler Endpoint() {
		return new FetchClusterNodeHumanCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchJsonTreeCommandHandler fetchJsonTreeEndpoint() {
		return new FetchJsonTreeCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchOriginCommandHandler fetchOriginEndpoint() {
		return new FetchOriginCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchSimpleClusterNodeCommandHandler fetchSimpleClusterNodeEndpoint() {
		return new FetchSimpleClusterNodeCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchSystemStatusCommandHandler fetchSystemStatusEndpoint() {
		return new FetchSystemStatusCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	FetchTreeCommandHandler fetchTreeEndpoint() {
		return new FetchTreeCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	ModifyRulesCommandHandler modifyRulesEndpoint() {
		return new ModifyRulesCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	SendMetricCommandHandler sendMetricEndpoint() {
		return new SendMetricCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	GetParamFlowRulesCommandHandler getParamFlowRules() {
		return new GetParamFlowRulesCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	ModifyParamFlowRulesCommandHandler modifyParamFlowRules() {
		return new ModifyParamFlowRulesCommandHandler();
	}

	@Bean
	@ConditionalOnMissingBean
	VersionCommandHandler versionEndpoint() {
		return new VersionCommandHandler();
	}
	
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(DispatcherServletsMappingDescriptionProvider.class)
	static class SpringMvcConfiguration {

		@Bean
		@ConditionalOnAvailableEndpoint
		public ResourceCommandHandler resourceCommandHandler(ApplicationContext applicationContext,
				DispatcherServletsMappingDescriptionProvider descriptionProviders, WebEndpointProperties webEndpointProperties) {
			return new ResourceCommandHandler(webEndpointProperties.getBasePath(), descriptionProviders, applicationContext);
		}
		
	}
	
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnClass(EurekaClientConfig.class)
	static class EurekaClientConfiguration {

		@Autowired
		private ApplicationContext context;

		@Autowired
		private AbstractDiscoveryClientOptionalArgs<?> optionalArgs;

		@Bean(destroyMethod = "shutdown")
		@ConditionalOnMissingBean(value = ApplicationInfoManager.class, search = SearchStrategy.CURRENT)
		public EurekaClient eurekaClient(ApplicationInfoManager manager, EurekaClientConfig config) {
			Map<String, String> appMetadata = new HashMap<String, String>();
			appMetadata.put("hostname", HostNameUtil.getHostName());
			appMetadata.put("apptype", String.valueOf(SentinelConfig.getAppType()));
			appMetadata.put("pid", String.valueOf(PidUtil.getPid()));
			manager.registerAppMetadata(appMetadata);
			return new CloudEurekaClient(manager, config, this.optionalArgs, this.context);
		}

//		@Bean
//		@ConditionalOnMissingBean(value = ApplicationInfoManager.class,
//				search = SearchStrategy.CURRENT)
//		public ApplicationInfoManager eurekaApplicationInfoManager(
//				EurekaInstanceConfig config) {
//			InstanceInfo instanceInfo = new InstanceInfoFactory().create(config);
//			return new ApplicationInfoManager(config, instanceInfo);
//		}

	}

}

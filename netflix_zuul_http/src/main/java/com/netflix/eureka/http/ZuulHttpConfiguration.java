package com.netflix.eureka.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.ZuulProxyMarkerConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.netflix.eureka.http.cache.IRouteCache;
import com.netflix.eureka.http.cache.IRouteLocator;
import com.netflix.eureka.http.handler.GetGatewayApiDefinitionGroupCommandHandler;
import com.netflix.eureka.http.handler.GetGatewayApiRuleCommandHandler;
import com.netflix.eureka.http.handler.SetGatewayApiDefinitionGroupCommandHandler;
import com.netflix.eureka.http.handler.SetGatewayApiRuleCommandHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "eureka.security.enabled", matchIfMissing = true)
@AutoConfigureBefore(ZuulProxyMarkerConfiguration.class)
@PropertySource("classpath:/spring.properties")
public class ZuulHttpConfiguration {
	
	@Bean
	@ConditionalOnAvailableEndpoint
	GetGatewayApiRuleCommandHandler getGatewayRuleCommandHandler() {
		return new GetGatewayApiRuleCommandHandler();
	}
	
	@Bean
	@ConditionalOnAvailableEndpoint
	SetGatewayApiRuleCommandHandler putGatewayRuleCommandHandler() {
		return new SetGatewayApiRuleCommandHandler();
	}
	
	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties({ ZuulProperties.class, ServerProperties.class })
	protected static class EndpointConfiguration {
		@Autowired
		protected ZuulProperties properties;

		@Autowired
		protected ServerProperties servers;
		
		@Bean
		@ConditionalOnMissingBean(IRouteLocator.class)
		IRouteCache getIRouteCache() {
			return new IRouteLocator(servers, properties);
		}
		
		@Bean
		@ConditionalOnAvailableEndpoint
		GetGatewayApiDefinitionGroupCommandHandler getGatewayApiDefinitionGroupCommandHandler(IRouteCache iRouteCache) {
			return new GetGatewayApiDefinitionGroupCommandHandler(iRouteCache);
		}
		
		@Bean
		@ConditionalOnAvailableEndpoint
		SetGatewayApiDefinitionGroupCommandHandler putGatewayApiDefinitionGroupCommandHandler(IRouteCache iRouteCache) {
			return new SetGatewayApiDefinitionGroupCommandHandler(iRouteCache);
		}
	}
}

package com.netflix.eureka.http;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.ZuulProxyMarkerConfiguration;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.netflix.eureka.http.cache.IRouteLocator;
import com.netflix.eureka.http.handler.GetGatewayApiDefinitionGroupCommandHandler;
import com.netflix.eureka.http.handler.GetGatewayApiRuleCommandHandler;
import com.netflix.eureka.http.handler.SetGatewayApiDefinitionGroupCommandHandler;
import com.netflix.eureka.http.handler.SetGatewayApiRuleCommandHandler;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(value = "eureka.sentinel.enabled", matchIfMissing = true)
@EnableConfigurationProperties({ ZuulProperties.class, ServerProperties.class })
@AutoConfigureBefore(ZuulProxyMarkerConfiguration.class)
@PropertySource("classpath:/spring.properties")
public class ZuulHttpConfiguration {
	
	@Autowired
	protected ZuulProperties properties;

	@Autowired
	protected ServerProperties servers;
	
	@Bean
	@ConditionalOnAvailableEndpoint
	GetGatewayApiDefinitionGroupCommandHandler getGatewayApiDefinitionGroupCommandHandler() {
		return new GetGatewayApiDefinitionGroupCommandHandler();
	}
	
	@Bean
	@ConditionalOnAvailableEndpoint
	GetGatewayApiRuleCommandHandler getGatewayRuleCommandHandler() {
		return new GetGatewayApiRuleCommandHandler();
	}
	
	@Bean
	@ConditionalOnAvailableEndpoint
	SetGatewayApiDefinitionGroupCommandHandler putGatewayApiDefinitionGroupCommandHandler() {
		return new SetGatewayApiDefinitionGroupCommandHandler(
				new IRouteLocator(servers, properties));
	}
	
	@Bean
	@ConditionalOnAvailableEndpoint
	SetGatewayApiRuleCommandHandler putGatewayRuleCommandHandler() {
		return new SetGatewayApiRuleCommandHandler();
	}
	
    
}

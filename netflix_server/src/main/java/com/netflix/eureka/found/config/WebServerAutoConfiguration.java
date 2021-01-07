package com.netflix.eureka.found.config;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.server.EurekaDashboardProperties;
import org.springframework.cloud.netflix.eureka.server.EurekaServerAutoConfiguration;
import org.springframework.cloud.netflix.eureka.server.EurekaServerInitializerConfiguration;
import org.springframework.cloud.netflix.eureka.server.InstanceRegistryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.eureka.EurekaServerConfig;
import com.netflix.eureka.dashboard.client.HttpapiClient;
import com.netflix.eureka.found.registry.ProvingInstanceRegistry;
import com.netflix.eureka.found.registry.SecretProperties;
import com.netflix.eureka.found.registry.ServiceGenerator;
import com.netflix.eureka.found.sentinel.ServerContext;
import com.netflix.eureka.found.service.ServerContextImpl;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.resources.ServerCodecs;

@Configuration(proxyBeanMethods = false)
@Import(WebServerInitializerConfiguration.class)
@AutoConfigureAfter(EurekaServerInitializerConfiguration.class)
@EnableConfigurationProperties({ 
	EurekaDashboardProperties.class,
	InstanceRegistryProperties.class,
	SecretProperties.class})
@PropertySource("classpath:/eureka/server.properties")
public class WebServerAutoConfiguration extends EurekaServerAutoConfiguration {
	
	@Autowired
	private EurekaServerConfig eurekaServerConfig;

	@Autowired
	private EurekaClientConfig eurekaClientConfig;
	
	@Autowired
	private EurekaClient eurekaClient;
	
	@Autowired
	private InstanceRegistryProperties instanceRegistryProperties;
	
	@Autowired(required = false)
	private static ServiceGenerator generator;
	
	@Inject
	WebServerAutoConfiguration (SecretProperties secretProperties) {
		generator = new ServiceGenerator(secretProperties);
	}
	
	@Bean
	public HttpapiClient getHttpapiClient() {
		return new HttpapiClient();
	}
	
	@Bean
	public ServerContext iServerContext(PeerAwareInstanceRegistry registry, HttpapiClient sentinelApi) {
		return new ServerContextImpl(registry, generator, sentinelApi);
	}
	
	@Override
	public PeerAwareInstanceRegistry peerAwareInstanceRegistry(ServerCodecs serverCodecs) {
		this.eurekaClient.getApplications(); // force initialization
		return new ProvingInstanceRegistry(generator, this.eurekaServerConfig, this.eurekaClientConfig,
				serverCodecs, this.eurekaClient,
				this.instanceRegistryProperties.getExpectedNumberOfClientsSendingRenews(),
				this.instanceRegistryProperties.getDefaultOpenForTrafficCount());
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
	
	@PostConstruct
	public void preprocess() {
		if(generator != null) {
			generator.init(); // force initialization
		}
	}
}

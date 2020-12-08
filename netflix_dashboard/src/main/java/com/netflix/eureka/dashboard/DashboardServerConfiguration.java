package com.netflix.eureka.dashboard;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration(proxyBeanMethods = false)
@PropertySource("classpath:/spring.properties")
public class DashboardServerConfiguration {
	
    
}

package com.netflix.eureka.found.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.eureka.server.EurekaServerInitializerConfiguration;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.netflix.eureka.found.sentinel.ServerContext;

@Configuration(proxyBeanMethods = false)
public class WebServerInitializerConfiguration implements SmartLifecycle, Ordered {
	private static final Logger log = LoggerFactory.getLogger(EurekaServerInitializerConfiguration.class);
	
	private boolean running;
	private int order = 1;
	
	@Autowired
	private ServerContext serverContext;
	
	@Override
	public void start() {
		new Thread(() -> {
			try {
				this.running = true;
				serverContext.initialize();
			} catch (Exception ex) {
				log.error("Could not initialize Web servlet context", ex);
			}
		}).start();
		
	}

	@Override
	public void stop() {
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	@Override
	public int getOrder() {
		return order;
	}
}

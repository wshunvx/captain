package com.netflix.eureka.found.registry;

import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.netflix.eureka.found.registry.SecretProperties.PREFIX;

@ConfigurationProperties(PREFIX)
public class SecretProperties {
	public static final String PREFIX = "eureka.secret";
	
	private String seed = "";

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}
	
}

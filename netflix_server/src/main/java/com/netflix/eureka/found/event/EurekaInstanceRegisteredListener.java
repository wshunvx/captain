package com.netflix.eureka.found.event;

import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.context.ApplicationListener;

public class EurekaInstanceRegisteredListener implements ApplicationListener<EurekaInstanceRegisteredEvent> {

	@Override
	public void onApplicationEvent(EurekaInstanceRegisteredEvent event) {
		
	}

}

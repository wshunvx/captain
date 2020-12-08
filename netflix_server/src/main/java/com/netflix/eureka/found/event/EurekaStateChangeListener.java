package com.netflix.eureka.found.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceCanceledEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRegisteredEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaInstanceRenewedEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaRegistryAvailableEvent;
import org.springframework.cloud.netflix.eureka.server.event.EurekaServerStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EurekaStateChangeListener {
	private static Logger log = LoggerFactory.getLogger(EurekaStateChangeListener.class);
	
	@EventListener
    public void listen(EurekaInstanceCanceledEvent event) {
//        log.info("EurekaInstanceCanceledEvent " + event);
    }
	
	@EventListener
    public void listen(EurekaInstanceRegisteredEvent event) {
//        log.info("Eureka Client start " + event);
    }
	
	@EventListener
    public void listen(EurekaInstanceRenewedEvent event) {
//        log.info("Eureka Client heartbeat  " + event);
    }
	
	@EventListener
    public void listen(EurekaRegistryAvailableEvent event) {
//        log.info("Eureka Service start  " + event);
    }
	
	@EventListener
    public void listen(EurekaServerStartedEvent event) {
//        log.info("Eureka Service status  " + event);
    }
}

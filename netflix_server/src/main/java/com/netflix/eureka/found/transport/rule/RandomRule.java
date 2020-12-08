package com.netflix.eureka.found.transport.rule;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.InstanceInfo.InstanceStatus;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.loadbalancer.RoundRobinRule;

public abstract class RandomRule implements AuthRule {
	private AtomicInteger nextServerCyclicCounter;

	private static final String APP_NAME = "GEN-AUTH";
	
    private static Logger log = LoggerFactory.getLogger(RoundRobinRule.class);

    public RandomRule() {
        nextServerCyclicCounter = new AtomicInteger(0);
    }
    
    public abstract PeerAwareInstanceRegistry getPeerAwareInstanceRegistry();

    public InstanceInfo choose(PeerAwareInstanceRegistry registry, String key) {
    	InstanceInfo server = null;
        if (registry == null) {
            log.warn("no load instance registry");
            return null;
        }
        
        Application application = registry.getApplication(key);
        if (application == null) {
            log.warn("no load application gen-auth");
            return null;
        }
        
        int count = 0;
        while (server == null && count++ < 10) {
            List<InstanceInfo> instanceInfos = application.getInstances();
            int serverCount = instanceInfos.size();

            if (serverCount == 0) {
                log.warn("No up servers available from load balancer: " + application);
                return null;
            }

            int nextServerIndex = incrementAndGetModulo(serverCount);
            server = instanceInfos.get(nextServerIndex);

            if (server == null) {
                /* Transient. */
                Thread.yield();
                continue;
            }

            if (server.getStatus() == InstanceStatus.UP) {
                return (server);
            }

            // Next.
            server = null;
        }

        if (count >= 10) {
            log.warn("No available alive servers after 10 tries from load balancer: "
                    + application);
        }
        return server;
    }

    private int incrementAndGetModulo(int modulo) {
        for (;;) {
            int current = nextServerCyclicCounter.get();
            int next = (current + 1) % modulo;
            if (nextServerCyclicCounter.compareAndSet(current, next)) {
                return next;
            }
        }
    }

    @Override
    public InstanceInfo choose() {
        return choose(getPeerAwareInstanceRegistry(), APP_NAME);
    }

}
